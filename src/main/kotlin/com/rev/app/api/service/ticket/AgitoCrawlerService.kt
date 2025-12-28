package com.rev.app.api.service.ticket

import com.rev.app.api.service.ticket.dto.PerformanceCreateRequest
import com.rev.app.domain.ticket.entity.PerformanceStatus
import com.rev.app.domain.ticket.repo.PerformanceRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import io.github.bonigarcia.wdm.WebDriverManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class AgitoCrawlerService(
    private val performanceRepository: PerformanceRepository,
    private val performanceService: PerformanceService
) {
    private val logger = LoggerFactory.getLogger(AgitoCrawlerService::class.java)
    
    // Agito 앱의 웹사이트 URL
    private val baseUrl = "https://azito.kr"
    
    data class AgitoPerformanceData(
        val title: String,
        val dateTime: LocalDateTime,
        val venue: String,
        val price: Int?,
        val description: String?,
        val imageUrl: String?,
        val detailUrl: String?
    )

    @Transactional
    fun crawlAgitoSchedules(clearExisting: Boolean = false) {
        logger.info("Agito 일정 크롤링 시작 (clearExisting=$clearExisting)")
        
        // 기존 데이터 삭제 옵션
        if (clearExisting) {
            val deletedCount = performanceRepository.count().toInt()
            performanceRepository.deleteAll()
            logger.info("기존 공연 데이터 ${deletedCount}개 삭제 완료")
        }
        
        try {
            val performances = fetchAgitoPerformances()
            logger.info("Agito에서 ${performances.size}개 공연 데이터 추출 완료")
            
            var createdCount = 0
            var updatedCount = 0
            var skippedCount = 0
            
            for (perfData in performances) {
                try {
                    // 중복 체크 (제목 + 날짜 + 장소)
                    val existing = performanceRepository.findAll().firstOrNull { existing ->
                        existing.title == perfData.title &&
                        existing.performanceDateTime == perfData.dateTime &&
                        existing.venue == perfData.venue
                    }
                    
                    if (existing != null) {
                        // 기존 데이터 업데이트 (가격 정보 등)
                        if (perfData.price != null && existing.price != perfData.price) {
                            existing.price = perfData.price
                            existing.updatedAt = java.time.Instant.now()
                            performanceRepository.save(existing)
                            updatedCount++
                            logger.debug("공연 업데이트: ${perfData.title} (가격: ${perfData.price}원)")
                        } else {
                            skippedCount++
                        }
                    } else {
                        // 새 공연 생성
                        val request = PerformanceCreateRequest(
                            title = perfData.title,
                            description = perfData.description,
                            venue = perfData.venue,
                            performanceDateTime = perfData.dateTime,
                            price = perfData.price ?: 30000, // 기본값 30000원
                            totalSeats = 100, // 기본값
                            imageUrl = perfData.imageUrl
                        )
                        performanceService.create(request)
                        createdCount++
                        logger.debug("새 공연 생성: ${perfData.title}")
                    }
                } catch (e: Exception) {
                    logger.warn("공연 저장 실패: ${perfData.title} - ${e.message}")
                }
            }
            
            logger.info("Agito 크롤링 완료: 생성=${createdCount}, 업데이트=${updatedCount}, 스킵=${skippedCount}")
        } catch (e: Exception) {
            logger.error("Agito 크롤링 실패", e)
            throw e
        }
    }

    private fun fetchAgitoPerformances(): List<AgitoPerformanceData> {
        val performances = mutableListOf<AgitoPerformanceData>()
        var driver: WebDriver? = null

        try {
            logger.info("Selenium을 사용한 Agito 크롤링 시작: $baseUrl")
            
            // WebDriver 설정 (Chrome)
            WebDriverManager.chromedriver().setup()
            
            val chromeOptions = ChromeOptions()
            chromeOptions.addArguments("--headless")
            chromeOptions.addArguments("--no-sandbox")
            chromeOptions.addArguments("--disable-dev-shm-usage")
            chromeOptions.addArguments("--disable-gpu")
            chromeOptions.addArguments("--window-size=1920,1080")
            chromeOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            
            driver = ChromeDriver(chromeOptions)
            val wait = WebDriverWait(driver, Duration.ofSeconds(20))
            
            // 페이지 로드
            driver.get(baseUrl)
            Thread.sleep(5000) // 페이지 로드 대기
            
            // JavaScript로 렌더링된 콘텐츠 대기
            wait.until { (it as JavascriptExecutor).executeScript("return document.readyState") == "complete" }
            Thread.sleep(3000) // 추가 대기
            
            // 페이지 구조 확인을 위한 로깅
            val pageSource = driver.pageSource
            val doc = Jsoup.parse(pageSource, baseUrl)
            logger.info("페이지 제목: ${doc.title()}")
            logger.info("페이지 본문 샘플 (처음 1000자): ${doc.body()?.text()?.take(1000)}")
            
            // JavaScript로 렌더링된 콘텐츠에서 데이터 추출 시도
            val jsExecutor = driver as JavascriptExecutor
            
            // 1. JavaScript 변수나 전역 객체에서 공연 데이터 추출 시도
            try {
                val jsData = jsExecutor.executeScript("""
                    // Agito 앱이 사용하는 데이터 구조 확인
                    if (typeof window.__NEXT_DATA__ !== 'undefined') {
                        return JSON.stringify(window.__NEXT_DATA__);
                    }
                    if (typeof window.__INITIAL_STATE__ !== 'undefined') {
                        return JSON.stringify(window.__INITIAL_STATE__);
                    }
                    if (typeof window.appData !== 'undefined') {
                        return JSON.stringify(window.appData);
                    }
                    return null;
                """)
                
                if (jsData != null) {
                    logger.info("JavaScript 데이터 발견: ${jsData.toString().take(500)}")
                    // JSON 파싱하여 공연 데이터 추출 (구조에 따라 수정 필요)
                }
            } catch (e: Exception) {
                logger.debug("JavaScript 데이터 추출 실패: ${e.message}")
            }
            
            // 2. DOM에서 공연 목록 요소 찾기 (다양한 선택자 시도)
            val selectors = listOf(
                ".performance-item, .event-item, .schedule-item, .concert-item",
                "[data-performance], [data-event], [data-concert]",
                "article, .card, .item",
                "[class*='performance'], [class*='event'], [class*='schedule'], [class*='concert']",
                ".list-item, .grid-item, .timeline-item"
            )
            
            var foundElements = false
            
            for (selector in selectors) {
                try {
                    val seleniumElements = driver.findElements(By.cssSelector(selector))
                    if (seleniumElements.isNotEmpty()) {
                        logger.info("선택자 '$selector'로 ${seleniumElements.size}개 요소 발견")
                        foundElements = true
                        
                        for (seleniumElement in seleniumElements) {
                            try {
                                val elementText = seleniumElement.text
                                val elementHtml = seleniumElement.getAttribute("outerHTML")
                                
                                if (elementText.isNotBlank() && elementText.length > 10) {
                                    val elementDoc = Jsoup.parse(elementHtml)
                                    val perfData = parseAgitoElement(elementDoc.body(), driver)
                                    if (perfData != null) {
                                        // 중복 체크
                                        if (!performances.any { it.title == perfData.title && it.dateTime == perfData.dateTime && it.venue == perfData.venue }) {
                                            performances.add(perfData)
                                            logger.debug("공연 데이터 추가: ${perfData.title}")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                logger.warn("요소 파싱 실패: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("선택자 '$selector' 실패: ${e.message}")
                }
            }
            
            // 3. 페이지 소스에서 직접 파싱 (백업 방법)
            if (!foundElements || performances.isEmpty()) {
                logger.info("Selenium 요소를 찾지 못해 페이지 소스에서 파싱 시도")
                val performanceElements = doc.select("a, article, .card, .item, [class*='event'], [class*='schedule']")
                
                for (element in performanceElements) {
                    try {
                        val text = element.text()
                        if (text.length > 20 && (text.contains("공연") || text.contains("콘서트") || text.contains("라이브") || 
                            Regex("""\d{4}[.\-/]\d{1,2}[.\-/]\d{1,2}""").find(text) != null)) {
                            val perfData = parseAgitoElement(element, driver)
                            if (perfData != null) {
                                if (!performances.any { it.title == perfData.title && it.dateTime == perfData.dateTime && it.venue == perfData.venue }) {
                                    performances.add(perfData)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // 무시
                    }
                }
            }
            
        } catch (e: Exception) {
            logger.error("Agito 크롤링 중 오류 발생", e)
        } finally {
            driver?.quit()
        }
        
        return performances.distinctBy { "${it.title}_${it.dateTime}_${it.venue}" }
    }

    private fun parseAgitoElement(element: Element, driver: WebDriver?): AgitoPerformanceData? {
        try {
            // 제목 추출
            val title = element.select("h1, h2, h3, h4, .title, [class*='title'], [data-title]").firstOrNull()?.text()
                ?: element.select("a").firstOrNull()?.text()
                ?: element.ownText().take(100)
            
            if (title.isBlank()) {
                return null
            }
            
            // 날짜 추출
            val dateText = element.select(".date, .datetime, [class*='date'], [data-date], time").firstOrNull()?.text()
                ?: element.select("[datetime]").firstOrNull()?.attr("datetime")
                ?: element.text().let { text ->
                    Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""").find(text)?.value
                }
            
            val dateTime = parseDateTime(dateText ?: "")
            
            // 장소 추출
            val venue = element.select(".venue, .location, [class*='venue'], [class*='location'], [data-venue]").firstOrNull()?.text()
                ?: element.text().let { text ->
                    Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""").find(text)?.groupValues?.get(1)
                }
                ?: "미정"
            
            // 가격 추출
            val priceText = element.select(".price, .cost, [class*='price'], [data-price]").firstOrNull()?.text()
                ?: element.text().let { text ->
                    Regex("""(\d{1,3}(?:,\d{3})*)\s*원""").find(text)?.groupValues?.get(1)
                }
            
            val price = priceText?.replace(",", "")?.toIntOrNull()
            
            // 설명 추출
            val description = element.select(".description, .desc, [class*='description'], [class*='desc']").firstOrNull()?.text()
                ?: element.select("p").firstOrNull()?.text()
            
            // 이미지 URL 추출
            val imageUrl = element.select("img").firstOrNull()?.attr("src")
                ?: element.select("[style*='background-image']").firstOrNull()?.attr("style")?.let { style ->
                    Regex("""url\(['"]?([^'")]+)['"]?\)""").find(style)?.groupValues?.get(1)
                }
            
            // 상세 페이지 URL 추출
            val detailUrl = element.select("a").firstOrNull()?.attr("href")
                ?: element.parent()?.select("a").firstOrNull()?.attr("href")
            
            return AgitoPerformanceData(
                title = title.trim(),
                dateTime = dateTime ?: LocalDateTime.now().plusDays(1).withHour(19).withMinute(0),
                venue = venue.trim(),
                price = price,
                description = description?.trim(),
                imageUrl = imageUrl,
                detailUrl = detailUrl?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
            )
        } catch (e: Exception) {
            logger.warn("Agito 요소 파싱 실패: ${e.message}")
            return null
        }
    }

    private fun parseDateTime(dateStr: String): LocalDateTime? {
        if (dateStr.isBlank()) return null
        
        return try {
            when {
                // ISO 8601 형식
                dateStr.contains("T") -> {
                    val hasTimeZone = dateStr.endsWith("Z") || dateStr.contains("+") || (dateStr.length > 19 && (dateStr[19] == '-' || dateStr[19] == '+'))
                    if (hasTimeZone) {
                        java.time.Instant.parse(dateStr).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
                    } else {
                        java.time.LocalDateTime.parse(dateStr)
                    }
                }
                // YYYY-MM-DD 형식
                dateStr.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> {
                    LocalDate.parse(dateStr).atTime(19, 0)
                }
                // 한국어 형식: 2024년 12월 30일
                dateStr.contains("년") && dateStr.contains("월") && dateStr.contains("일") -> {
                    val yearMatch = Regex("""(\d{4})년""").find(dateStr)
                    val monthMatch = Regex("""(\d{1,2})월""").find(dateStr)
                    val dayMatch = Regex("""(\d{1,2})일""").find(dateStr)
                    val hourMatch = Regex("""(\d{1,2})시""").find(dateStr)
                    val minuteMatch = Regex("""(\d{1,2})분""").find(dateStr)
                    
                    if (yearMatch != null && monthMatch != null && dayMatch != null) {
                        val year = yearMatch.groupValues[1].toInt()
                        val month = monthMatch.groupValues[1].toInt()
                        val day = dayMatch.groupValues[1].toInt()
                        val hour = hourMatch?.groupValues?.get(1)?.toInt() ?: 19
                        val minute = minuteMatch?.groupValues?.get(1)?.toInt() ?: 0
                        LocalDateTime.of(year, month, day, hour, minute)
                    } else {
                        null
                    }
                }
                else -> {
                    // 다양한 형식 시도
                    val formatters = listOf(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREA),
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREA),
                        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.KOREA),
                        DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.KOREA),
                    )
                    
                    for (formatter in formatters) {
                        try {
                            return LocalDateTime.parse(dateStr, formatter)
                        } catch (e: Exception) {
                            // 다음 포맷 시도
                        }
                    }
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("날짜 파싱 실패: $dateStr - ${e.message}")
            null
        }
    }
}


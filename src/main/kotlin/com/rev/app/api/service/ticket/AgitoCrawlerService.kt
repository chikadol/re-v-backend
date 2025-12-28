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
    
    // Agito 앱의 웹사이트 URL - 최신 오픈 라이브 페이지
    private val baseUrl = "https://azito.kr/latest-open"
    
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
        
        // 기존 데이터 백업 (삭제 전 개수 확인)
        val existingCount = performanceRepository.count().toInt()
        logger.info("현재 DB에 저장된 공연 수: $existingCount")
        
        // 기존 데이터 삭제 옵션
        if (clearExisting) {
            performanceRepository.deleteAll()
            performanceRepository.flush()
            logger.info("기존 공연 데이터 ${existingCount}개 삭제 완료")
        }
        
        try {
            val performances = fetchAgitoPerformances()
            logger.info("Agito에서 ${performances.size}개 공연 데이터 추출 완료")
            
            // 메인 페이지에서 가격 정보를 더 정확하게 추출
            // azito.kr은 앱 전용이므로 상세 페이지 접근이 불가능함
            // 메인 페이지의 각 요소에서 가격 정보를 더 정확하게 파싱
            val performancesWithPrice = performances.map { perfData ->
                // 가격이 없거나 기본값(30000)인 경우, 메인 페이지에서 다시 추출 시도
                if (perfData.price == null || perfData.price == 30000) {
                    logger.debug("가격 정보가 없는 공연: ${perfData.title}, 메인 페이지에서 재추출 시도")
                    // 여기서는 이미 메인 페이지에서 추출했으므로 그대로 사용
                    perfData
                } else {
                    perfData
                }
            }
            
            var createdCount = 0
            var updatedCount = 0
            var skippedCount = 0
            
            for (perfData in performancesWithPrice) {
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
                            logger.info("공연 업데이트: ${perfData.title} (가격: ${perfData.price}원)")
                        } else {
                            skippedCount++
                        }
                    } else {
                        // 새 공연 생성 - 가격이 없으면 기본값 사용하지 않고 경고
                        if (perfData.price == null) {
                            logger.warn("가격 정보가 없는 공연: ${perfData.title}, 기본값 30000원 사용")
                        }
                        val request = PerformanceCreateRequest(
                            title = perfData.title,
                            description = perfData.description,
                            venue = perfData.venue,
                            performanceDateTime = perfData.dateTime,
                            price = perfData.price ?: 30000, // 기본값 30000원 (가격 정보가 없을 때만)
                            totalSeats = 100, // 기본값
                            imageUrl = perfData.imageUrl
                        )
                        performanceService.create(request)
                        createdCount++
                        logger.info("새 공연 생성: ${perfData.title} (가격: ${perfData.price ?: 30000}원)")
                    }
                } catch (e: Exception) {
                    logger.warn("공연 저장 실패: ${perfData.title} - ${e.message}")
                }
            }
            
            logger.info("Agito 크롤링 완료: 생성=${createdCount}, 업데이트=${updatedCount}, 스킵=${skippedCount}")
            
            // 추출된 데이터 요약 로깅
            val priceStats = performancesWithPrice.groupBy { 
                when {
                    it.price == null -> "가격 없음"
                    it.price == 30000 -> "기본값(30000원)"
                    it.price in 5000..500000 -> "정상 가격(${it.price}원)"
                    else -> "이상한 가격(${it.price}원)"
                }
            }
            logger.info("=== 가격 정보 통계 ===")
            priceStats.forEach { (category, list) ->
                logger.info("$category: ${list.size}개")
                if (category == "정상 가격") {
                    val priceRange = list.map { it.price!! }
                    logger.info("  가격 범위: ${priceRange.minOrNull()}원 ~ ${priceRange.maxOrNull()}원")
                }
            }
            
            // 추출된 정보 요약
            val withTitle = performancesWithPrice.count { it.title.isNotBlank() }
            val withDate = performancesWithPrice.count { it.dateTime != null }
            val withVenue = performancesWithPrice.count { it.venue.isNotBlank() && it.venue != "미정" }
            val withPrice = performancesWithPrice.count { it.price != null && it.price != 30000 }
            val withDescription = performancesWithPrice.count { !it.description.isNullOrBlank() }
            val withImage = performancesWithPrice.count { !it.imageUrl.isNullOrBlank() }
            
            logger.info("=== 추출된 정보 통계 ===")
            logger.info("총 공연 수: ${performancesWithPrice.size}")
            logger.info("제목 있음: $withTitle (${(withTitle * 100 / performancesWithPrice.size)}%)")
            logger.info("날짜 있음: $withDate (${(withDate * 100 / performancesWithPrice.size)}%)")
            logger.info("장소 있음: $withVenue (${(withVenue * 100 / performancesWithPrice.size)}%)")
            logger.info("가격 있음(정상): $withPrice (${(withPrice * 100 / performancesWithPrice.size)}%)")
            logger.info("설명 있음: $withDescription (${(withDescription * 100 / performancesWithPrice.size)}%)")
            logger.info("이미지 있음: $withImage (${(withImage * 100 / performancesWithPrice.size)}%)")
            
            // 최종 결과 확인
            val finalCount = performanceRepository.count().toInt()
            logger.info("크롤링 후 DB에 저장된 총 공연 수: $finalCount")
            
            if (finalCount == 0 && clearExisting) {
                logger.error("⚠️ 경고: 크롤링 후 데이터가 없습니다! 기존 데이터가 삭제되었지만 새 데이터를 가져오지 못했습니다.")
                logger.error("페이지 구조를 확인하고 크롤링 로직을 수정해야 합니다.")
            }
        } catch (e: Exception) {
            logger.error("Agito 크롤링 실패", e)
            logger.error("크롤링 실패로 인해 데이터가 손실되었을 수 있습니다. 로그를 확인하세요.")
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
            logger.info("페이지 로드 시작: $baseUrl")
            Thread.sleep(8000) // 페이지 로드 대기 (동적 콘텐츠 로딩 시간 확보)
            
            // JavaScript 실행을 위한 JavascriptExecutor 선언
            val jsExecutor = driver as JavascriptExecutor
            
            // JavaScript로 렌더링된 콘텐츠 대기
            wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }
            Thread.sleep(5000) // 추가 대기 (동적 콘텐츠 렌더링 대기)
            
            // 스크롤하여 지연 로딩 콘텐츠 활성화
            try {
                jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);")
                Thread.sleep(2000)
                jsExecutor.executeScript("window.scrollTo(0, 0);")
                Thread.sleep(2000)
            } catch (e: Exception) {
                logger.debug("스크롤 실패: ${e.message}")
            }
            
            // 페이지 구조 확인을 위한 로깅
            val pageSource = driver.pageSource
            val doc = Jsoup.parse(pageSource, baseUrl)
            logger.info("=== Agito 크롤링 페이지 분석 ===")
            logger.info("페이지 URL: ${driver.currentUrl}")
            logger.info("페이지 제목: ${doc.title()}")
            logger.info("페이지 본문 전체 길이: ${doc.body()?.text()?.length ?: 0}자")
            logger.info("페이지 본문 샘플 (처음 2000자): ${doc.body()?.text()?.take(2000)}")
            
            // 페이지의 모든 링크와 클래스 확인
            val allLinks = doc.select("a[href]")
            logger.info("페이지 내 링크 개수: ${allLinks.size}")
            if (allLinks.size > 0) {
                logger.info("샘플 링크 5개: ${allLinks.take(5).map { "${it.text().take(50)} -> ${it.attr("href")}" }.joinToString(", ")}")
            }
            
            val allClasses = doc.select("[class]").map { it.className() }.distinct().take(20)
            logger.info("페이지 내 주요 클래스 (최대 20개): ${allClasses.joinToString(", ")}")
            
            // 1. JavaScript 변수나 전역 객체에서 공연 데이터 추출 시도
            try {
                val jsData = jsExecutor.executeScript("""
                    var data = {};
                    
                    // Next.js 데이터 확인
                    if (typeof window.__NEXT_DATA__ !== 'undefined') {
                        try {
                            var nextData = window.__NEXT_DATA__;
                            data.nextData = JSON.stringify(nextData).substring(0, 5000);
                            // props나 pageProps에서 공연 데이터 찾기
                            if (nextData.props && nextData.props.pageProps) {
                                data.pageProps = JSON.stringify(nextData.props.pageProps).substring(0, 5000);
                            }
                        } catch(e) {
                            data.nextDataError = e.message;
                        }
                    }
                    
                    // React/Redux 상태 확인
                    if (typeof window.__INITIAL_STATE__ !== 'undefined') {
                        data.initialState = JSON.stringify(window.__INITIAL_STATE__).substring(0, 5000);
                    }
                    
                    // 앱 데이터 확인
                    if (typeof window.appData !== 'undefined') {
                        data.appData = JSON.stringify(window.appData).substring(0, 5000);
                    }
                    
                    // React 컴포넌트 트리에서 데이터 찾기
                    if (typeof window.__REACT_DEVTOOLS_GLOBAL_HOOK__ !== 'undefined') {
                        data.hasReactDevTools = true;
                    }
                    
                    // 모든 전역 변수 확인
                    var globalVars = [];
                    for (var key in window) {
                        if (key.includes('data') || key.includes('event') || key.includes('schedule') || 
                            key.includes('performance') || key.includes('concert') || key.includes('live')) {
                            try {
                                var value = window[key];
                                if (typeof value === 'object' && value !== null) {
                                    globalVars.push(key + ' (object)');
                                } else {
                                    globalVars.push(key);
                                }
                            } catch(e) {
                                globalVars.push(key + ' (error)');
                            }
                        }
                    }
                    data.globalVars = globalVars;
                    
                    // DOM에서 data 속성 확인
                    var dataAttributes = [];
                    var elementsWithData = document.querySelectorAll('[data-event], [data-performance], [data-concert], [data-live]');
                    for (var i = 0; i < Math.min(10, elementsWithData.length); i++) {
                        var el = elementsWithData[i];
                        var attrs = {};
                        for (var j = 0; j < el.attributes.length; j++) {
                            var attr = el.attributes[j];
                            if (attr.name.startsWith('data-')) {
                                attrs[attr.name] = attr.value.substring(0, 100);
                            }
                        }
                        if (Object.keys(attrs).length > 0) {
                            dataAttributes.push(attrs);
                        }
                    }
                    data.dataAttributes = dataAttributes;
                    
                    return JSON.stringify(data);
                """)
                
                if (jsData != null) {
                    logger.info("JavaScript 데이터 발견: ${jsData.toString().take(2000)}")
                    // 전체 데이터를 디버그 로그로 출력
                    logger.debug("전체 JavaScript 데이터: ${jsData.toString()}")
                } else {
                    logger.info("JavaScript 전역 데이터 없음")
                }
            } catch (e: Exception) {
                logger.warn("JavaScript 데이터 추출 실패: ${e.message}")
            }
            
            // 2. DOM에서 공연 목록 요소 찾기 (다양한 선택자 시도)
            val selectors = listOf(
                ".performance-item, .event-item, .schedule-item, .concert-item",
                "[data-performance], [data-event], [data-concert]",
                "article, .card, .item",
                "[class*='performance'], [class*='event'], [class*='schedule'], [class*='concert']",
                ".list-item, .grid-item, .timeline-item",
                "div[class*='event'], div[class*='schedule'], div[class*='concert']",
                "li, .list-item, .grid-item"
            )
            
            var foundElements = false
            var totalElementsFound = 0
            
            for (selector in selectors) {
                try {
                    val seleniumElements = driver.findElements(By.cssSelector(selector))
                    if (seleniumElements.isNotEmpty()) {
                        logger.info("선택자 '$selector'로 ${seleniumElements.size}개 요소 발견")
                        totalElementsFound += seleniumElements.size
                        foundElements = true
                        
                        // 처음 10개 요소의 텍스트 샘플 로깅 (더 상세하게)
                        for (i in 0 until minOf(10, seleniumElements.size)) {
                            try {
                                val element = seleniumElements[i]
                                val sampleText = element.text.take(300)
                                val html = element.getAttribute("outerHTML").take(500)
                                val classes = element.getAttribute("class") ?: ""
                                val dataAttrs = element.getAttribute("outerHTML")?.let { html ->
                                    Regex("""data-[^=]+="[^"]*"""").findAll(html).map { it.value }.joinToString(", ")
                                } ?: ""
                                logger.info("샘플 요소 #$i (선택자: $selector):")
                                logger.info("  텍스트: $sampleText")
                                logger.info("  클래스: $classes")
                                logger.info("  data 속성: $dataAttrs")
                                logger.info("  HTML (처음 500자): $html")
                            } catch (e: Exception) {
                                logger.debug("샘플 요소 로깅 실패: ${e.message}")
                            }
                        }
                        
                        for (seleniumElement in seleniumElements) {
                            try {
                                val elementText = seleniumElement.text
                                val elementHtml = seleniumElement.getAttribute("outerHTML")
                                
                                if (elementText.isNotBlank() && elementText.length > 10) {
                                    val elementDoc = Jsoup.parse(elementHtml)
                                    val perfData = parseAgitoElement(elementDoc.body(), driver)
                                    if (perfData != null && perfData.title.isNotBlank() && perfData.title.length > 3) {
                                        // 중복 체크
                                        if (!performances.any { it.title == perfData.title && it.dateTime == perfData.dateTime && it.venue == perfData.venue }) {
                                            performances.add(perfData)
                                            val priceInfo = if (perfData.price != null) " (가격: ${perfData.price}원)" else " (가격 정보 없음)"
                                            logger.info("공연 데이터 추가: ${perfData.title} (${perfData.dateTime})$priceInfo")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                logger.debug("요소 파싱 실패: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("선택자 '$selector' 실패: ${e.message}")
                }
            }
            
            logger.info("총 ${totalElementsFound}개 요소 발견, ${performances.size}개 공연 데이터 추출")
            
            // 3. 페이지 소스에서 직접 파싱 (백업 방법)
            if (!foundElements || performances.isEmpty()) {
                logger.warn("Selenium 요소를 찾지 못했거나 공연 데이터가 없음. 페이지 소스에서 파싱 시도")
                
                // 모든 텍스트가 있는 요소 확인 (더 넓은 범위)
                val allTextElements = doc.select("div, article, section, li, a, span, p, h1, h2, h3, h4, h5").filter { 
                    val text = it.text()
                    text.length > 15 && 
                    (text.contains("공연") || text.contains("콘서트") || text.contains("라이브") || 
                     text.contains("일정") || text.contains("스케줄") || text.contains("OPEN") || text.contains("START") ||
                     Regex("""\d{4}[.\-/]\d{1,2}[.\-/]\d{1,2}""").find(text) != null ||
                     Regex("""\d{1,2}월\s*\d{1,2}일""").find(text) != null ||
                     Regex("""OPEN\s+\d{1,2}:\d{2}""").find(text) != null ||
                     Regex("""START\s+\d{1,2}:\d{2}""").find(text) != null)
                }
                
                logger.info("텍스트 기반으로 ${allTextElements.size}개 후보 요소 발견")
                
                // 처음 20개 요소의 샘플 로깅
                for (i in 0 until minOf(20, allTextElements.size)) {
                    try {
                        val element = allTextElements[i]
                        val text = element.text().take(200)
                        val tag = element.tagName()
                        val classes = element.className()
                        logger.debug("후보 요소 #$i [$tag] (클래스: $classes): $text")
                    } catch (e: Exception) {
                        // 무시
                    }
                }
                
                for (element in allTextElements.take(200)) { // 최대 200개까지 처리
                    try {
                        val text = element.text()
                        if (text.length > 15) {
                            val perfData = parseAgitoElement(element, driver)
                            if (perfData != null && perfData.title.isNotBlank() && perfData.title.length > 3) {
                                if (!performances.any { it.title == perfData.title && it.dateTime == perfData.dateTime && it.venue == perfData.venue }) {
                                    performances.add(perfData)
                                    val priceInfo = if (perfData.price != null) " (가격: ${perfData.price}원)" else " (가격 정보 없음)"
                                    logger.info("페이지 소스에서 공연 데이터 추가: ${perfData.title}$priceInfo")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // 무시
                    }
                }
            }
            
            if (performances.isEmpty()) {
                logger.error("공연 데이터를 전혀 추출하지 못했습니다. 페이지 구조를 확인해야 합니다.")
                logger.error("페이지 HTML 구조 (처음 5000자): ${pageSource.take(5000)}")
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
            val elementText = element.text()
            
            // 제목 추출 (다양한 패턴 시도)
            val title = element.select("h1, h2, h3, h4, h5, .title, [class*='title'], [data-title], .event-title, .performance-title").firstOrNull()?.text()?.trim()
                ?: element.select("a").firstOrNull()?.text()?.trim()
                ?: element.select("strong, b").firstOrNull()?.text()?.trim()
                ?: elementText.lines().firstOrNull()?.trim()?.take(100)
            
            if (title.isNullOrBlank() || title.length < 3) {
                return null
            }
            
            // 날짜 추출 (다양한 형식 지원) - 더 정확하게 추출
            val dateText = element.select(".date, .datetime, [class*='date'], [class*='time'], [data-date], time, .event-date").firstOrNull()?.text()?.trim()
                ?: element.select("[datetime]").firstOrNull()?.attr("datetime")
                ?: element.select("[data-datetime]").firstOrNull()?.attr("data-datetime")
                ?: elementText.let { text ->
                    // 다양한 날짜 패턴 시도 (우선순위 순서)
                    // 1. YYYY.MM.DD 형식 (예: 2025.12.31)
                    Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""").find(text)?.value
                        // 2. MM월 DD일 형식 (예: 12월 31일)
                        ?: Regex("""(\d{1,2})월\s*(\d{1,2})일""").find(text)?.value
                        // 3. YYYY년 MM월 DD일 형식
                        ?: Regex("""(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일""").find(text)?.value
                        // 4. MM/DD 형식
                        ?: Regex("""(\d{1,2})[/](\d{1,2})""").find(text)?.value
                }
            
            // 시간 추출 (OPEN/START 시간 포함)
            val timeText = element.select(".time, [class*='time'], .event-time").firstOrNull()?.text()?.trim()
                ?: elementText.let { text ->
                    // "OPEN 18:20 | START 18:40" 형식에서 START 시간 우선 추출
                    val startMatch = Regex("""START\s+(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE).find(text)
                    if (startMatch != null) {
                        "${startMatch.groupValues[1]}:${startMatch.groupValues[2]}"
                    } else {
                        // OPEN 시간 추출
                        val openMatch = Regex("""OPEN\s+(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE).find(text)
                        if (openMatch != null) {
                            "${openMatch.groupValues[1]}:${openMatch.groupValues[2]}"
                        } else {
                            // 일반 시간 형식
                            Regex("""(\d{1,2}):(\d{2})""").find(text)?.value
                        }
                    }
                }
            
            // 날짜 파싱 (날짜가 없으면 null 반환, 기본값 사용 안 함)
            val dateTime = if (dateText.isNullOrBlank()) {
                logger.warn("날짜 정보를 찾을 수 없음: ${title.take(50)}")
                null
            } else {
                parseDateTime(dateText, timeText)
            }
            
            // 날짜가 없으면 이 요소는 건너뛰기
            if (dateTime == null) {
                logger.debug("날짜 파싱 실패로 요소 건너뜀: ${title.take(50)}")
                return null
            }
            
            // 장소 추출
            val venue = element.select(".venue, .location, [class*='venue'], [class*='location'], [data-venue], .event-venue").firstOrNull()?.text()?.trim()
                ?: elementText.let { text ->
                    Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""").find(text)?.groupValues?.get(1)?.trim()
                        ?: Regex("""장소[:\s]*([가-힣a-zA-Z0-9\s]+)""").find(text)?.groupValues?.get(1)?.trim()
                        ?: Regex("""공연장[:\s]*([가-힣a-zA-Z0-9\s]+)""").find(text)?.groupValues?.get(1)?.trim()
                }
                ?: "미정"
            
            // 가격 추출 (다양한 패턴) - 메인 페이지에서 정확하게 추출
            val priceText = element.select(".price, .cost, [class*='price'], [data-price], .event-price, .ticket-price").firstOrNull()?.text()?.trim()
                ?: elementText.let { text ->
                    // 1. "ADV 18,000 / DOOR 20,000" 형식 (ADV 우선)
                    val advDoorMatch = Regex("""ADV\s+(\d{1,3}(?:,\d{3})*)\s*[/]\s*DOOR\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE).find(text)
                    if (advDoorMatch != null) {
                        return@let advDoorMatch.groupValues[1] // ADV 가격 우선
                    }
                    // 2. "ADV 18,000" 형식
                    val advOnlyMatch = Regex("""ADV\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE).find(text)
                    if (advOnlyMatch != null) {
                        return@let advOnlyMatch.groupValues[1]
                    }
                    // 3. "DOOR 20,000" 형식
                    val doorOnlyMatch = Regex("""DOOR\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE).find(text)
                    if (doorOnlyMatch != null) {
                        return@let doorOnlyMatch.groupValues[1]
                    }
                    // 4. 일반 가격 패턴들
                    Regex("""가격[:\s]*(\d{1,3}(?:,\d{3})*)\s*원""").find(text)?.groupValues?.get(1)
                        ?: Regex("""입장료[:\s]*(\d{1,3}(?:,\d{3})*)\s*원""").find(text)?.groupValues?.get(1)
                        ?: Regex("""예매[:\s]*(\d{1,3}(?:,\d{3})*)\s*원""").find(text)?.groupValues?.get(1)
                        ?: Regex("""(\d{1,3}(?:,\d{3})*)\s*원""").find(text)?.groupValues?.get(1)
                        ?: Regex("""₩\s*(\d{1,3}(?:,\d{3})*)""").find(text)?.groupValues?.get(1)
                        ?: Regex("""(\d{4,6})\s*원""").find(text)?.groupValues?.get(1)
                }
            
            val price = priceText?.replace(",", "")?.trim()?.toIntOrNull()
            
            // 설명 추출
            val description = element.select(".description, .desc, [class*='description'], [class*='desc']").firstOrNull()?.text()?.trim()
                ?: element.select("p").firstOrNull()?.text()?.trim()
            
            // 이미지 URL 추출
            val imageUrl = element.select("img").firstOrNull()?.attr("src")
                ?: element.select("[style*='background-image']").firstOrNull()?.attr("style")?.let { style ->
                    Regex("""url\(['"]?([^'")]+)['"]?\)""").find(style)?.groupValues?.get(1)
                }
            
            // 상세 페이지 URL 추출
            val detailUrl = element.select("a").firstOrNull()?.attr("href")
                ?: (element.parent()?.select("a")?.firstOrNull()?.attr("href"))
            
            val perfData = AgitoPerformanceData(
                title = title.trim(),
                dateTime = dateTime ?: LocalDateTime.now().plusDays(1).withHour(19).withMinute(0),
                venue = venue.trim(),
                price = price,
                description = description?.trim(),
                imageUrl = imageUrl,
                detailUrl = detailUrl?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
            )
            
            // 가격이 추출되었는지 로깅
            if (perfData.price != null && perfData.price in 5000..500000) {
                logger.debug("가격 추출 성공: ${perfData.title} -> ${perfData.price}원")
            } else if (perfData.price != null) {
                logger.warn("추출된 가격이 범위를 벗어남: ${perfData.title} -> ${perfData.price}원 (범위: 5000-500000)")
            } else {
                logger.debug("가격 정보 없음: ${perfData.title}")
            }
            
            return perfData
        } catch (e: Exception) {
            logger.warn("Agito 요소 파싱 실패: ${e.message}")
            return null
        }
    }
    
    private fun extractPriceFromDetailPage(detailUrl: String, driver: WebDriver): Int? {
        return try {
            logger.info("상세 페이지에서 가격 추출 시도: $detailUrl")
            
            driver.get(detailUrl)
            val jsExecutor = driver as JavascriptExecutor
            
            // 페이지 로드 대기
            Thread.sleep(3000)
            
            // document.readyState 확인
            try {
                val readyState = jsExecutor.executeScript("return document.readyState") as String?
                logger.debug("상세 페이지 로드 상태: $readyState")
                if (readyState != "complete") {
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                logger.debug("readyState 확인 실패: ${e.message}")
            }
            
            Thread.sleep(2000) // 추가 대기
            
            val pageSource = driver.pageSource
            val doc = Jsoup.parse(pageSource, detailUrl)
            val bodyText = doc.body().text()
            
            logger.info("상세 페이지 본문 텍스트 (ADV/DOOR 검색용, 처음 2000자): ${bodyText.take(2000)}")
            
            // 1. ADV (예매) / DOOR (현장) 형식 우선 추출
            val advDoorPatterns = listOf(
                // 공백과 슬래시 사이의 다양한 조합
                Regex("""ADV\s+(\d{1,3}(?:,\d{3})*)\s*[/]\s*DOOR\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                // 공백 없이 슬래시만: "ADV 18,000/DOOR 20,000"
                Regex("""ADV\s+(\d{1,3}(?:,\d{3})*)/DOOR\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                // 쉼표 없음: "ADV 18000 / DOOR 20000"
                Regex("""ADV\s+(\d{4,6})\s*[/]\s*DOOR\s+(\d{4,6})""", RegexOption.IGNORE_CASE),
                // 콜론 포함: "ADV: 18,000 / DOOR: 20,000"
                Regex("""ADV\s*:\s*(\d{1,3}(?:,\d{3})*)\s*[/]\s*DOOR\s*:\s*(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                // 가장 유연한 패턴 (모든 공백/구분자 조합 허용)
                Regex("""ADV\s*[:\s]*(\d{1,3}(?:,\d{3})*)\s*[/]\s*DOOR\s*[:\s]*(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
            )
            
            for ((index, advDoorPattern) in advDoorPatterns.withIndex()) {
                val advDoorMatch = advDoorPattern.find(bodyText)
                if (advDoorMatch != null) {
                    try {
                        // ADV (예매 가격)을 우선 사용
                        val advPriceStr = advDoorMatch.groupValues[1].replace(",", "").trim()
                        val advPrice = advPriceStr.toInt()
                        
                        logger.info("ADV/DOOR 패턴 #${index + 1} 매칭 성공: ADV=${advPriceStr}원, DOOR=${advDoorMatch.groupValues[2]}원 (URL: $detailUrl)")
                        
                        if (advPrice in 5000..500000) {
                            logger.info("ADV/DOOR 패턴에서 예매 가격 추출 성공: ${advPrice}원 (URL: $detailUrl)")
                            return advPrice
                        } else {
                            logger.warn("추출된 가격이 범위를 벗어남: ${advPrice}원 (범위: 5000-500000)")
                        }
                    } catch (e: Exception) {
                        logger.warn("ADV 가격 파싱 실패: ${e.message}")
                    }
                }
            }
            
            logger.debug("ADV/DOOR 패턴 매칭 실패 - 다른 패턴 시도")
            
            // 2. 개별 ADV 또는 DOOR 패턴 찾기 (ADV/DOOR 조합이 없는 경우)
            val advOnlyPatterns = listOf(
                Regex("""ADV\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                Regex("""ADV\s*:\s*(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                Regex("""ADV[:\s]*(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                Regex("""ADV\s*(\d{4,6})""", RegexOption.IGNORE_CASE), // 쉼표 없음
            )
            
            for ((index, advOnlyPattern) in advOnlyPatterns.withIndex()) {
                val advOnlyMatch = advOnlyPattern.find(bodyText)
                if (advOnlyMatch != null) {
                    try {
                        val advPriceStr = advOnlyMatch.groupValues[1].replace(",", "").trim()
                        val advPrice = advPriceStr.toInt()
                        if (advPrice in 5000..500000) {
                            logger.info("ADV 패턴 #${index + 1}에서 예매 가격 추출 성공: ${advPrice}원 (URL: $detailUrl)")
                            return advPrice
                        }
                    } catch (e: Exception) {
                        logger.warn("ADV 가격 파싱 실패: ${e.message}")
                    }
                }
            }
            
            // 3. DOOR 패턴도 확인 (ADV가 없는 경우 DOOR을 사용)
            val doorOnlyPatterns = listOf(
                Regex("""DOOR\s+(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                Regex("""DOOR\s*:\s*(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
                Regex("""DOOR[:\s]*(\d{1,3}(?:,\d{3})*)""", RegexOption.IGNORE_CASE),
            )
            
            for ((index, doorOnlyPattern) in doorOnlyPatterns.withIndex()) {
                val doorOnlyMatch = doorOnlyPattern.find(bodyText)
                if (doorOnlyMatch != null) {
                    try {
                        val doorPriceStr = doorOnlyMatch.groupValues[1].replace(",", "").trim()
                        val doorPrice = doorPriceStr.toInt()
                        if (doorPrice in 5000..500000) {
                            logger.info("DOOR 패턴 #${index + 1}에서 현장 가격 추출 성공: ${doorPrice}원 (URL: $detailUrl)")
                            return doorPrice
                        }
                    } catch (e: Exception) {
                        logger.warn("DOOR 가격 파싱 실패: ${e.message}")
                    }
                }
            }
            
            // 4. 일반 가격 패턴 찾기 (ADV/DOOR 패턴이 없는 경우 백업)
            val pricePatterns = listOf(
                Regex("""가격[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 가격: 30,000원
                Regex("""입장료[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 입장료: 30,000원
                Regex("""티켓[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 티켓: 30,000원
                Regex("""예매[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 예매: 30,000원
                Regex("""(\d{1,3}(?:,\d{3})*)\s*원"""),  // 30,000원 (일반)
                Regex("""₩\s*(\d{1,3}(?:,\d{3})*)"""),  // ₩30,000
                Regex("""(\d{4,6})\s*원"""),  // 30000원
            )
            
            var maxPrice: Int? = null
            
            for (pattern in pricePatterns) {
                val priceMatches = pattern.findAll(bodyText)
                for (priceMatch in priceMatches) {
                    try {
                        val priceStr = priceMatch.groupValues[1].replace(",", "").trim()
                        val parsedPrice = priceStr.toInt()
                        if (parsedPrice in 5000..500000) { // 합리적인 가격 범위
                            if (maxPrice == null || parsedPrice > maxPrice) {
                                maxPrice = parsedPrice
                            }
                        }
                    } catch (e: Exception) {
                        // 무시
                    }
                }
            }
            
            if (maxPrice != null) {
                logger.info("일반 가격 패턴에서 가격 추출 성공: ${maxPrice}원 (URL: $detailUrl)")
            } else {
                logger.warn("모든 가격 패턴에서 가격을 찾지 못함 (URL: $detailUrl)")
            }
            maxPrice
        } catch (e: Exception) {
            logger.warn("상세 페이지 가격 추출 실패: $detailUrl - ${e.message}")
            null
        }
    }

    private fun parseDateTime(dateStr: String, timeStr: String? = null): LocalDateTime? {
        if (dateStr.isBlank()) {
            logger.debug("날짜 문자열이 비어있음")
            return null
        }
        
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
                // YYYY-MM-DD 형식 (예: 2025-12-31)
                dateStr.matches(Regex("""\d{4}-\d{1,2}-\d{1,2}""")) -> {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt()
                        val day = parts[2].toInt()
                        val time = parseTime(timeStr)
                        LocalDateTime.of(year, month, day, time.first, time.second)
                    } else {
                        null
                    }
                }
                // YYYY.MM.DD 형식 (예: 2025.12.31) - 가장 일반적
                dateStr.matches(Regex("""\d{4}\.\d{1,2}\.\d{1,2}""")) -> {
                    val parts = dateStr.split(".")
                    if (parts.size == 3) {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt()
                        val day = parts[2].toInt()
                        val time = parseTime(timeStr)
                        val result = LocalDateTime.of(year, month, day, time.first, time.second)
                        logger.debug("날짜 파싱 성공 (YYYY.MM.DD): $dateStr -> $result")
                        result
                    } else {
                        logger.warn("날짜 파싱 실패 (YYYY.MM.DD 형식이지만 파트가 3개가 아님): $dateStr")
                        null
                    }
                }
                // YYYY/MM/DD 형식
                dateStr.matches(Regex("""\d{4}/\d{1,2}/\d{1,2}""")) -> {
                    val parts = dateStr.split("/")
                    if (parts.size == 3) {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt()
                        val day = parts[2].toInt()
                        val time = parseTime(timeStr)
                        LocalDateTime.of(year, month, day, time.first, time.second)
                    } else {
                        null
                    }
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
                        val hour = hourMatch?.groupValues?.get(1)?.toInt() ?: parseTime(timeStr).first
                        val minute = minuteMatch?.groupValues?.get(1)?.toInt() ?: parseTime(timeStr).second
                        LocalDateTime.of(year, month, day, hour, minute)
                    } else {
                        null
                    }
                }
                // MM월 DD일 형식 (예: 12월 31일)
                dateStr.matches(Regex("""\d{1,2}월\s*\d{1,2}일""")) -> {
                    val monthMatch = Regex("""(\d{1,2})월""").find(dateStr)
                    val dayMatch = Regex("""(\d{1,2})일""").find(dateStr)
                    if (monthMatch != null && dayMatch != null) {
                        val now = LocalDateTime.now()
                        val month = monthMatch.groupValues[1].toInt()
                        val day = dayMatch.groupValues[1].toInt()
                        val time = parseTime(timeStr)
                        // 올해 또는 내년으로 추정
                        val year = if (month < now.monthValue || (month == now.monthValue && day < now.dayOfMonth)) {
                            now.year + 1
                        } else {
                            now.year
                        }
                        val result = LocalDateTime.of(year, month, day, time.first, time.second)
                        logger.debug("날짜 파싱 성공 (MM월 DD일): $dateStr -> $result (추정 연도: $year)")
                        result
                    } else {
                        null
                    }
                }
                // MM/DD 형식 (예: 12/31)
                dateStr.matches(Regex("""\d{1,2}/\d{1,2}""")) -> {
                    val parts = dateStr.split("/")
                    if (parts.size == 2) {
                        val now = LocalDateTime.now()
                        val month = parts[0].toInt()
                        val day = parts[1].toInt()
                        val time = parseTime(timeStr)
                        // 올해 또는 내년으로 추정
                        val year = if (month < now.monthValue || (month == now.monthValue && day < now.dayOfMonth)) {
                            now.year + 1
                        } else {
                            now.year
                        }
                        LocalDateTime.of(year, month, day, time.first, time.second)
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
                            val fullDateStr = if (timeStr != null && !dateStr.contains(" ")) {
                                "$dateStr $timeStr"
                            } else {
                                dateStr
                            }
                            val result = LocalDateTime.parse(fullDateStr, formatter)
                            logger.debug("날짜 파싱 성공 (포맷터 사용): $fullDateStr -> $result")
                            return result
                        } catch (e: Exception) {
                            // 다음 포맷 시도
                        }
                    }
                    logger.warn("모든 날짜 포맷 시도 실패: dateStr=$dateStr, timeStr=$timeStr")
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("날짜 파싱 실패: dateStr=$dateStr, timeStr=$timeStr - ${e.message}")
            null
        }
    }
    
    private fun parseTime(timeStr: String?): Pair<Int, Int> {
        if (timeStr.isNullOrBlank()) return Pair(19, 0) // 기본값 19:00
        
        return try {
            val timeMatch = Regex("""(\d{1,2}):(\d{2})""").find(timeStr)
            if (timeMatch != null) {
                Pair(timeMatch.groupValues[1].toInt(), timeMatch.groupValues[2].toInt())
            } else {
                val hourMatch = Regex("""(\d{1,2})시""").find(timeStr)
                val minuteMatch = Regex("""(\d{1,2})분""").find(timeStr)
                val hour = hourMatch?.groupValues?.get(1)?.toInt() ?: 19
                val minute = minuteMatch?.groupValues?.get(1)?.toInt() ?: 0
                Pair(hour, minute)
            }
        } catch (e: Exception) {
            Pair(19, 0)
        }
    }
}

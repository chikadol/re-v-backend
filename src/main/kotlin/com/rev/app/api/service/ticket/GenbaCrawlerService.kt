package com.rev.app.api.service.ticket

import com.rev.app.domain.ticket.entity.PerformanceEntity
import com.rev.app.domain.ticket.entity.PerformanceStatus
import com.rev.app.domain.ticket.repo.PerformanceRepository
import com.rev.app.domain.idol.IdolRepository
import com.rev.app.domain.idol.IdolEntity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.JavascriptExecutor
import io.github.bonigarcia.wdm.WebDriverManager
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.UUID

@Service
class GenbaCrawlerService(
    private val performanceRepository: PerformanceRepository,
    private val performanceService: PerformanceService,
    private val idolRepository: IdolRepository
) {
    private val logger = LoggerFactory.getLogger(GenbaCrawlerService::class.java)
    private val baseUrl = "https://chikadol.net/genba"

    // 매시간 실행 (크롤링 빈도 조절 가능)
    // cron 표현식: 초 분 시 일 월 요일
    // @Scheduled(cron = "0 0 * * * *") // 매 시간 정각 - 주석 처리 (수동 실행)
    // @Scheduled(fixedRate = 3600000) // 1시간마다 (밀리초)
    @Transactional
    fun crawlGenbaSchedules(clearExisting: Boolean = false, fast: Boolean = true) {
        logger.info("겐바 일정 크롤링 시작 (clearExisting=$clearExisting, fast=$fast)")
        
        // 기존 데이터 삭제 옵션
        if (clearExisting) {
            val deletedCount = performanceRepository.count().toInt()
            performanceRepository.deleteAll()
            performanceRepository.flush()
            logger.info("기존 공연 데이터 ${deletedCount}개 삭제 완료")
        }
        
        try {
            val performances = fetchGenbaPerformances()
            logger.info("크롤링으로 추출된 공연 수: ${performances.size}")
            
            // 추출된 데이터의 날짜 분포 확인
            val dateDistribution = performances.groupBy { it.performanceDateTime.toLocalDate() }
            logger.info("날짜별 분포: ${dateDistribution.map { "${it.key}(${it.value.size}개)" }.joinToString(", ")}")
            
            if (performances.isEmpty()) {
                logger.warn("크롤링 결과가 비어있습니다. 웹사이트 구조 확인이 필요합니다.")
                logger.warn("참고: chikadol.net/genba는 JavaScript로 동적 렌더링되므로 Jsoup만으로는 데이터 추출이 어려울 수 있습니다.")
                logger.warn("대안: Selenium/Playwright 같은 브라우저 자동화 도구 사용을 고려하거나, 테스트 데이터 생성 엔드포인트 사용: POST /api/performances/test-data")
                return
            }

            var created = 0
            var updated = 0
            var skipped = 0

            // 크롤링된 데이터에서 중복 제거 (같은 제목+일시+장소는 하나만)
            val uniquePerformances = performances.distinctBy { 
                "${it.title}_${it.performanceDateTime}_${it.venue}" 
            }
            
            logger.info("중복 제거 전: ${performances.size}개, 중복 제거 후: ${uniquePerformances.size}개")
            
                    // 상세 페이지에서 가격 정보 추출 (모든 공연에 대해 detailUrl이 있으면 재추출)
                    // 먼저 출연진은 Jsoup으로 모두 시도 (fast 여부와 무관)
                    val withPerformers = uniquePerformances.map { pd ->
                        val performers = pd.detailUrl?.let { extractPerformersFromDetail(it) } ?: emptyList()
                        pd.copy(performers = performers)
                    }

                    val performancesWithPrice = if (fast) {
                        logger.info("fast 모드: 가격 추출만 건너뛰고 출연진은 Jsoup으로 반영합니다.")
                        withPerformers
                    } else {
                        val list = mutableListOf<GenbaPerformanceData>()
                        var tempDriver: WebDriver? = null
                        try {
                            if (withPerformers.any { it.detailUrl != null }) {
                                logger.info("상세 페이지에서 가격 정보 추출 시작 (총 ${withPerformers.count { it.detailUrl != null }}개 공연)")
                                WebDriverManager.chromedriver().setup()
                                val chromeOptions = ChromeOptions()
                                chromeOptions.addArguments("--headless")
                                chromeOptions.addArguments("--no-sandbox")
                                chromeOptions.addArguments("--disable-dev-shm-usage")
                                chromeOptions.addArguments("--disable-gpu")
                                tempDriver = ChromeDriver(chromeOptions)
                            }

                            for (performanceData in withPerformers) {
                                var finalPrice = performanceData.price

                                if (performanceData.detailUrl != null && tempDriver != null) {
                                    val extractedPrice = extractPriceFromDetailPage(tempDriver, performanceData.detailUrl)
                                    if (extractedPrice != null) {
                                        finalPrice = extractedPrice
                                        logger.info("상세 페이지에서 가격 추출: ${performanceData.title} - ${finalPrice}원 (이전: ${performanceData.price ?: "없음"})")
                                    }
                                }

                                list.add(performanceData.copy(price = finalPrice))
                            }
                        } catch (e: Exception) {
                            logger.warn("상세 페이지 추출 중 오류 (기본값 사용): ${e.message}")
                            list.addAll(withPerformers)
                        } finally {
                            try {
                                tempDriver?.quit()
                            } catch (e: Exception) {
                                logger.warn("임시 WebDriver 종료 중 오류: ${e.message}")
                            }
                        }
                        list
                    }
                    
                    for (performanceData in performancesWithPrice) {
                try {
                    val idolId = resolveIdolId(performanceData.performers, performanceData.title)
                    // 중복 체크: 제목과 일시만으로 비교 (장소는 "미정"일 수 있어서 제외)
                    // 제목이 정확히 같고, 날짜/시간이 정확히 같은 경우만 중복으로 판단
                    val existing = performanceRepository.findAll().find { 
                        it.title == performanceData.title && 
                        it.performanceDateTime == performanceData.performanceDateTime
                    }

                    if (existing == null) {
                        // 새로운 공연 추가
                        val createdPerformance = performanceService.create(
                            com.rev.app.api.service.ticket.dto.PerformanceCreateRequest(
                                title = performanceData.title,
                                description = performanceData.description,
                                venue = performanceData.venue,
                                performanceDateTime = performanceData.performanceDateTime,
                                price = performanceData.price ?: 30000, // 기본 가격 3만원 (크롤링에서 추출하지 못한 경우)
                                totalSeats = performanceData.totalSeats ?: 200, // 스탠딩이므로 기본 200명
                                    imageUrl = performanceData.imageUrl,
                                    idolId = idolId
                            )
                        )
                        created++
                        logger.info("새 공연 추가: ${performanceData.title} @ ${performanceData.venue} - ${performanceData.performanceDateTime} (가격: ${performanceData.price ?: 30000}원)")
                    } else {
                        // 기존 공연이 있으면 가격이나 장소 정보 업데이트 (더 정확한 정보로)
                        var needUpdate = false
                        
                        // 가격 업데이트 (크롤링한 가격이 있고, 기존과 다르면)
                        if (performanceData.price != null && existing.price != performanceData.price) {
                            existing.price = performanceData.price
                            needUpdate = true
                            logger.info("가격 업데이트: ${performanceData.title} - ${existing.price}원 -> ${performanceData.price}원")
                        }
                        
                        // 장소 업데이트 (크롤링한 장소가 "미정"이 아니고, 기존과 다르면)
                        if (performanceData.venue != "미정" && existing.venue != performanceData.venue) {
                            existing.venue = performanceData.venue
                            needUpdate = true
                            logger.info("장소 업데이트: ${performanceData.title} - ${existing.venue} -> ${performanceData.venue}")
                        }
                        
                        if (needUpdate) {
                            performanceRepository.save(existing)
                            updated++
                            logger.info("공연 업데이트: ${performanceData.title}")
                        } else {
                            skipped++
                            logger.debug("공연 건너뜀 (변경사항 없음): ${performanceData.title}")
                        }
                    }
                } catch (e: Exception) {
                    logger.error("공연 저장 실패: ${performanceData.title} - ${e.message}", e)
                }
            }

            logger.info("겐바 일정 크롤링 완료: 생성=$created, 업데이트=$updated, 건너뜀=$skipped")
        } catch (e: Exception) {
            logger.error("겐바 일정 크롤링 실패", e)
        }
    }

    /**
     * 공연 상세 페이지에서 가격 정보 추출
     * ADV (예매) / DOOR (현장) 형식도 지원
     */
    private fun extractPriceFromDetailPage(driver: WebDriver, detailUrl: String): Int? {
        return try {
            logger.info("상세 페이지 접속: $detailUrl")
            driver.get(detailUrl)
            Thread.sleep(5000) // 페이지 로딩 대기 (5초로 증가)
            
            // 페이지가 완전히 로드될 때까지 추가 대기
            val jsExecutor = driver as JavascriptExecutor
            try {
                val readyState = jsExecutor.executeScript("return document.readyState")
                logger.debug("상세 페이지 로드 상태: $readyState")
                Thread.sleep(2000) // 추가 대기
            } catch (e: Exception) {
                logger.debug("로드 상태 확인 실패: ${e.message}")
            }
            
            val pageSource = driver.pageSource
            val doc = Jsoup.parse(pageSource, detailUrl)
            
            val bodyText = doc.body().text()
            
            logger.info("상세 페이지 본문 텍스트 (ADV/DOOR 검색용, 처음 2000자): ${bodyText.take(2000)}")
            
            // 1. ADV (예매) / DOOR (현장) 형식 우선 추출
            // 실제 사이트 형식: "ADV 18,000 / DOOR 20,000" 또는 "ADV 18000 / DOOR 20000" 등
            // 다양한 패턴 시도 (더 유연한 패턴부터 시작)
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
            // 하지만 기본값 30000원이나 5000원 같은 값은 피해야 함
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
                        // 5000원이나 30000원은 기본값일 가능성이 높으므로 제외하거나 신중하게 처리
                        // 하지만 실제로 그 가격일 수도 있으므로 범위 체크만 수행
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

    private fun fetchGenbaPerformances(): List<GenbaPerformanceData> {
        val performances = mutableListOf<GenbaPerformanceData>()
        var driver: WebDriver? = null

        try {
            logger.info("Selenium을 사용한 크롤링 시작: $baseUrl")
            
            // WebDriver 설정 (Chrome)
            WebDriverManager.chromedriver().setup()
            
            val chromeOptions = ChromeOptions()
            chromeOptions.addArguments("--headless") // 헤드리스 모드 (백그라운드 실행)
            chromeOptions.addArguments("--no-sandbox")
            chromeOptions.addArguments("--disable-dev-shm-usage")
            chromeOptions.addArguments("--disable-gpu")
            chromeOptions.addArguments("--window-size=1920,1080")
            chromeOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            
            driver = ChromeDriver(chromeOptions)
            val wait = WebDriverWait(driver, Duration.ofSeconds(20))
            val jsExecutor = driver as JavascriptExecutor
            
            // 여러 달의 데이터를 수집하기 위해 월을 이동하면서 크롤링
            // 현재 월(12월), 이전 달들(11월, 10월 등), 다음 달들(1월, 2월 등)
            // 월 이동 순서: 현재(0) -> 이전(-1, -2) -> 다음(+1, +2)
            // 하지만 더 간단하게 현재 월에서 모든 이벤트를 한 번에 가져오는 것이 좋을 수도 있음
            // 일단 FullCalendar API가 모든 이벤트를 반환하는지 확인해보고, 안 되면 월 이동 방식 사용
            val monthsToCrawl = listOf(0, -1, -2, 1, 2) // 현재 월부터 시작해서 이전/다음 달로 이동
            
            for (monthOffset in monthsToCrawl) {
                try {
                    logger.info("크롤링 대상 월: 현재 기준 ${if (monthOffset >= 0) "+" else ""}$monthOffset 개월")
                    
                    // 페이지 로드 (각 월마다)
                    if (monthOffset == monthsToCrawl.first()) {
                        // 첫 번째 월(현재 월)이면 초기 로드
                        logger.info("페이지 초기 로드 중... (현재 월)")
                        driver.get(baseUrl)
                        Thread.sleep(8000) // 초기 로딩 대기 (8초로 증가)
                    } else {
                        // 이후 월들은 월 이동
                        logger.info("월 이동 중... (offset: $monthOffset)")
                        try {
                            // FullCalendar API로 월 변경
                            // monthOffset이 음수면 이전 달, 양수면 다음 달로 이동
                            val direction = if (monthOffset < 0) "prev" else "next"
                            val buttonSelector = if (monthOffset < 0) ".fc-prev-button, .fc-prevButton, button.fc-prev-button, .fc-button-prev" else ".fc-next-button, .fc-nextButton, button.fc-next-button, .fc-button-next"
                            
                            val success = jsExecutor.executeScript("""
                                try {
                                    if (typeof jQuery !== 'undefined') {
                                        var calendarElement = jQuery('#calendar');
                                        if (calendarElement.length > 0) {
                                            // FullCalendar v2/v3 방식
                                            try {
                                                calendarElement.fullCalendar('$direction');
                                                return true;
                                            } catch(e1) {
                                                // FullCalendar v4/v5 방식
                                                try {
                                                    var calendarElDom = document.getElementById('calendar');
                                                    if (calendarElDom && calendarElDom._fullCalendar) {
                                                        var calendarApi = calendarElDom._fullCalendar.getApi();
                                                        calendarApi.$direction();
                                                        return true;
                                                    }
                                                } catch(e2) {
                                                    console.error('월 변경 실패:', e2);
                                                    return false;
                                                }
                                            }
                                        }
                                    }
                                    return false;
                                } catch(e) {
                                    console.error('월 변경 오류:', e);
                                    return false;
                                }
                            """) as Boolean?
                            
                            if (success != true) {
                                // API 방식 실패 시 버튼 클릭 시도
                                try {
                                    val navButton = driver.findElements(By.cssSelector(buttonSelector))
                                    if (navButton.isNotEmpty()) {
                                        navButton.first().click()
                                        logger.info("${if (monthOffset < 0) "이전" else "다음"} 달 버튼 클릭 성공")
                                    } else {
                                        logger.warn("${if (monthOffset < 0) "이전" else "다음"} 달 버튼을 찾을 수 없음")
                                    }
                                } catch (e: Exception) {
                                    logger.warn("버튼 클릭 실패: ${e.message}")
                                }
                            }
                            
                            Thread.sleep(5000) // 월 변경 후 대기 (5초로 증가)
                        } catch (e: Exception) {
                            logger.warn("월 이동 실패 (계속 진행): ${e.message}")
                        }
                    }
                    
                    // 현재 월의 이벤트 수집 (아래 로직 계속)
                    logger.info("현재 표시된 월의 이벤트 수집 시작")
                    
                    // 캘린더가 로드될 때까지 대기
                    try {
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#calendar, .fc-event, .fc-day-grid-event, .fc-time-grid-event")))
                        logger.info("캘린더 요소 발견")
                    } catch (e: Exception) {
                        logger.warn("캘린더 요소 대기 시간 초과, 계속 진행: ${e.message}")
                    }
                    
                    Thread.sleep(5000) // 추가 대기 시간
                    
                    // 현재 월의 이벤트를 수집하는 로직
                    val monthPerformances = collectMonthEvents(driver, jsExecutor, wait)
                    logger.info("현재 월에서 ${monthPerformances.size}개 이벤트 수집 완료")
                    performances.addAll(monthPerformances)
                    
                } catch (e: Exception) {
                    logger.error("월 크롤링 중 오류 (계속 진행): ${e.message}", e)
                }
            }
            
            // 모든 월 수집 완료 후 중복 제거
            val uniquePerformances = performances.distinctBy { "${it.title}_${it.performanceDateTime}_${it.venue}" }
            logger.info("전체 수집: ${performances.size}개, 중복 제거 후: ${uniquePerformances.size}개")
            return uniquePerformances
            
        } catch (e: Exception) {
            logger.error("Selenium 크롤링 실패: ${e.message}", e)
        } finally {
            // WebDriver 종료
            try {
                driver?.quit()
                logger.info("WebDriver 종료 완료")
            } catch (e: Exception) {
                logger.warn("WebDriver 종료 중 오류: ${e.message}")
            }
        }

        logger.info("총 ${performances.size}개 공연 데이터 추출 완료")
        return performances.distinctBy { "${it.title}_${it.performanceDateTime}" }
    }
    
    /**
     * 현재 표시된 월의 이벤트를 수집하는 함수
     */
    private fun collectMonthEvents(driver: WebDriver, jsExecutor: JavascriptExecutor, wait: WebDriverWait): List<GenbaPerformanceData> {
        val performances = mutableListOf<GenbaPerformanceData>()
        
        try {
            // 페이지 로드 상태 확인
            try {
                val readyState = jsExecutor.executeScript("return document.readyState")
                logger.info("페이지 로드 상태: $readyState")
            } catch (e: Exception) {
                logger.warn("페이지 로드 상태 확인 실패: ${e.message}")
            }
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#calendar, .fc-event, .fc-day-grid-event, .fc-time-grid-event")))
                logger.info("캘린더 요소 발견")
            } catch (e: Exception) {
                logger.warn("캘린더 요소 대기 시간 초과, 계속 진행: ${e.message}")
            }
            
            // 추가 대기 시간 (JavaScript 실행 완료를 위해) - 더 길게 대기
            Thread.sleep(10000) // 10초 대기로 증가
            
            // 페이지가 완전히 로드될 때까지 추가 대기 (jsExecutor는 이미 위에서 선언됨)
            try {
                val readyState = jsExecutor.executeScript("return document.readyState")
                logger.info("페이지 로드 상태: $readyState")
                
                // FullCalendar가 로드되었는지 확인
                val calendarExists = jsExecutor.executeScript("""
                    return typeof jQuery !== 'undefined' && 
                           jQuery('#calendar').length > 0;
                """) as Boolean?
                logger.info("FullCalendar 존재 여부: $calendarExists")
            } catch (e: Exception) {
                logger.warn("페이지 로드 상태 확인 실패: ${e.message}")
            }
            
                // 페이지 HTML 구조 디버깅을 위해 일부 저장
                try {
                val calendarHtml = jsExecutor.executeScript("""
                    var calDomEl = document.getElementById('calendar');
                    if (calDomEl) {
                        return calDomEl.outerHTML.substring(0, 2000);
                    }
                    return 'calendar element not found';
                """) as String?
                logger.info("캘린더 HTML 일부 (2000자): $calendarHtml")
                
                // FullCalendar 이벤트가 실제로 로드되었는지 확인
                val eventCount = jsExecutor.executeScript("""
                    try {
                        if (typeof jQuery !== 'undefined') {
                            var calEl = jQuery('#calendar');
                            if (calEl.length > 0) {
                                var view = calEl.fullCalendar('getView');
                                if (view) {
                                    var calObj = view.calendar;
                                    var events = calObj ? calObj.clientEvents() : [];
                                    return events.length;
                                }
                            }
                        }
                        return -1;
                    } catch(e) {
                        return -2;
                    }
                """) as Int?
                logger.info("FullCalendar API를 통한 이벤트 개수: $eventCount")
            } catch (e: Exception) {
                logger.warn("캘린더 구조 확인 실패: ${e.message}")
            }
            
            // Selenium으로 직접 이벤트 요소 찾기 (추가 백업 방법)
                try {
                    logger.info("Selenium으로 DOM 요소 직접 파싱 시도 (추가 백업)")
                val eventElements = driver.findElements(By.cssSelector(".fc-event, .fc-day-grid-event, .fc-time-grid-event, [class*='event'], .fc-event-container .fc-event, .fc-content"))
                logger.info("Selenium으로 발견한 이벤트 요소 수: ${eventElements.size}")
                
                // 발견한 요소 중 몇 개의 실제 데이터를 로깅
                for (i in 0 until minOf(5, eventElements.size)) {
                    try {
                        val sampleEl = eventElements[i]
                        val sampleText = sampleEl.text.take(200)
                        val sampleAttrs = mapOf(
                            "data-start" to (sampleEl.getAttribute("data-start") ?: "없음"),
                            "data-date" to (sampleEl.getAttribute("data-date") ?: "없음"),
                            "class" to (sampleEl.getAttribute("class") ?: "없음")
                        )
                        logger.info("샘플 요소 #$i: text=${sampleText.take(100)}, attrs=$sampleAttrs")
                    } catch (e: Exception) {
                        logger.debug("샘플 요소 로깅 실패: ${e.message}")
                    }
                }
                
                for (element in eventElements.take(200)) { // 최대 200개만 처리
                    try {
                        val titleElement = element.findElements(By.cssSelector(".fc-title, .fc-event-title, .fc-content .fc-title, .fc-event-title-container, .fc-list-item-title")).firstOrNull()
                        var title = titleElement?.text?.takeIf { it.isNotBlank() } 
                            ?: element.findElements(By.cssSelector("a")).firstOrNull()?.text?.takeIf { it.isNotBlank() }
                            ?: element.text.take(100).trim()
                        
                        // 제목이 너무 짧거나 의미없으면 스킵
                        if (title.length < 3 || title.all { it.isWhitespace() || it.isDigit() || it in ".-/: " }) {
                            continue
                        }
                        
                        // elementText를 먼저 추출 (다른 곳에서도 사용)
                        val elementText = element.text.trim()
                        
                        // data 속성에서 날짜 정보 추출 (가장 정확한 방법)
                        // 여러 우선순위로 시도
                        var startAttr: String? = null
                        var startAttrSource = "none"
                        
                        // 1. 직접 data-start, start, data-event-start 속성 확인
                        startAttr = element.getAttribute("data-start")?.takeIf { it.isNotBlank() }
                            ?.also { startAttrSource = "data-start" }
                            ?: element.getAttribute("start")?.takeIf { it.isNotBlank() }
                                ?.also { startAttrSource = "start" }
                            ?: element.getAttribute("data-event-start")?.takeIf { it.isNotBlank() }
                                ?.also { startAttrSource = "data-event-start" }
                        
                        // 2. 부모 요소에서 data-date 확인 (캘린더 셀의 날짜) - 더 많은 조상 요소 확인
                        if (startAttr.isNullOrBlank()) {
                            try {
                                // 여러 조상 요소를 시도
                                var parent = element.findElement(By.xpath("./.."))
                                var depth = 0
                                while (parent != null && depth < 10) {
                                    val dataDate = parent.getAttribute("data-date")
                                    if (!dataDate.isNullOrBlank()) {
                                        startAttr = dataDate
                                        startAttrSource = "parent[${depth}].data-date"
                                        logger.debug("부모 요소에서 날짜 발견 (depth=$depth): $dataDate")
                                        break
                                    }
                                    try {
                                        parent = parent.findElement(By.xpath("./.."))
                                        depth++
                                    } catch (e: Exception) {
                                        break
                                    }
                                }
                                
                                // XPath로 직접 찾기 시도
                                if (startAttr.isNullOrBlank()) {
                                    try {
                                        val parentCell = element.findElement(By.xpath("./ancestor::td[@data-date] | ./ancestor::div[@data-date] | ./ancestor::*[@data-date]"))
                                        startAttr = parentCell.getAttribute("data-date")?.takeIf { it.isNotBlank() }
                                        startAttrSource = "xpath.ancestor.data-date"
                                    } catch (e: Exception) {
                                        // XPath로도 찾지 못함
                                    }
                                }
                            } catch (e: Exception) {
                                logger.debug("부모 요소에서 날짜 찾기 실패: ${e.message}")
                            }
                        }
                        
                        // 3. href에서 날짜 추출 시도
                        if (startAttr.isNullOrBlank()) {
                            val href = element.getAttribute("href")
                            if (!href.isNullOrBlank()) {
                                val hrefDateMatch = Regex("""/(\d{4})/(\d{1,2})/(\d{1,2})""").find(href)
                                startAttr = hrefDateMatch?.let { match ->
                                    startAttrSource = "href"
                                    "${match.groupValues[1]}-${match.groupValues[2].padStart(2, '0')}-${match.groupValues[3].padStart(2, '0')}"
                                }
                            }
                        }
                        
                        // 4. 텍스트에서 날짜 패턴 추출 (최후의 수단)
                        val titleDateMatch = Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""").find(elementText)
                        val titleDateStr = titleDateMatch?.let { 
                            if (startAttr.isNullOrBlank()) {
                                startAttrSource = "text"
                            }
                            "${it.groupValues[1]}-${it.groupValues[2].padStart(2, '0')}-${it.groupValues[3].padStart(2, '0')}"
                        }
                        
                        val finalStartAttr = startAttr ?: titleDateStr
                        
                        logger.debug("날짜 속성 추출: title=$title, source=$startAttrSource, finalStartAttr=$finalStartAttr")
                        
                        // 시간 정보 추출
                        val timeElement = element.findElements(By.cssSelector(".fc-time, .fc-event-time, .fc-list-item-time")).firstOrNull()
                        val timeText = timeElement?.text
                        
                        logger.debug("이벤트 요소 발견: title=$title, startAttr=$finalStartAttr, timeText=$timeText, elementText=${elementText.take(100)}")
                        
                        val dateTime = if (!finalStartAttr.isNullOrBlank()) {
                            try {
                                // ISO 8601 형식 또는 YYYY-MM-DD 형식 파싱
                                if (finalStartAttr.contains("T")) {
                                    // ISO 8601: 2024-12-30T19:00:00
                                    // 타임존 정보가 있으면 한국 시간으로 변환, 없으면 직접 파싱
                                    val hasTimeZone = finalStartAttr.endsWith("Z") || finalStartAttr.contains("+") || (finalStartAttr.length > 19 && (finalStartAttr[19] == '-' || finalStartAttr[19] == '+'))
                                    if (hasTimeZone) {
                                        java.time.Instant.parse(finalStartAttr).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
                                    } else {
                                        java.time.LocalDateTime.parse(finalStartAttr)
                                    }
                                } else if (finalStartAttr.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
                                    // YYYY-MM-DD 형식
                                    val date = java.time.LocalDate.parse(finalStartAttr)
                                    // 시간 정보가 있으면 파싱, 없으면 19:00 기본값
                                    val timeMatch = Regex("""(\d{1,2}):(\d{2})""").find(elementText ?: timeText ?: "")
                                    if (timeMatch != null) {
                                        date.atTime(timeMatch.groupValues[1].toInt(), timeMatch.groupValues[2].toInt())
                                    } else {
                                        date.atTime(19, 0)
                                    }
                                } else {
                                    // 다른 형식 시도
                                    parseDateTime(finalStartAttr + " " + (timeText ?: "")) ?: continue
                                }
                            } catch (e: Exception) {
                                logger.debug("날짜 파싱 실패 (startAttr): $finalStartAttr - ${e.message}")
                                val parsedTime = parseDateTime(elementText) ?: parseDateTime(timeText)
                                if (parsedTime == null) {
                                    logger.warn("날짜 파싱 실패, 스킵: title=$title, startAttr=$finalStartAttr")
                                    continue
                                }
                                parsedTime
                            }
                        } else {
                            // startAttr이 없으면 텍스트에서 날짜 추출
                            val parsedTime = parseDateTime(elementText) ?: parseDateTime(timeText)
                            if (parsedTime == null) {
                                logger.warn("날짜 정보 없음, 스킵: title=$title")
                                continue
                            }
                            parsedTime
                        }
                        
                        // 과거 날짜는 제외하지 않음 (12월 전체 데이터 수집을 위해)
                        // 단, 너무 과거(예: 1년 이상)는 제외
                        val oneYearAgo = LocalDateTime.now().minusYears(1)
                        if (dateTime.isBefore(oneYearAgo)) {
                            logger.debug("너무 과거 날짜 스킵: $dateTime")
                            continue
                        }
                        
                        // 장소 정보 추출 시도
                        var venue = "미정"
                        val venueMatch = Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""").find(elementText)
                        if (venueMatch != null) {
                            venue = venueMatch.groupValues[1].trim()
                        }
                        
                        // 가격 정보 추출 시도 (설명이나 텍스트, 제목에서 원 단위 가격 찾기)
                        var price: Int? = null
                        
                        // 1. data-price, price 속성 확인
                        val dataPrice = element.getAttribute("data-price")?.takeIf { it.isNotBlank() }
                        if (!dataPrice.isNullOrBlank()) {
                            try {
                                val parsedPrice = dataPrice.replace(",", "").replace("원", "").trim().toInt()
                                if (parsedPrice in 5000..500000) {
                                    price = parsedPrice
                                    logger.debug("가격 추출 성공 (data-price): ${price}원")
                                }
                            } catch (e: Exception) {
                                // 무시
                            }
                        }
                        
                        // 2. 텍스트에서 가격 패턴 찾기
                        if (price == null) {
                            val searchText = "$elementText $title" // 전체 텍스트에서 검색
                            val pricePatterns = listOf(
                                Regex("""(\d{1,3}(?:,\d{3})*)\s*원"""),  // 30,000원
                                Regex("""₩\s*(\d{1,3}(?:,\d{3})*)"""),  // ₩30,000
                                Regex("""(\d{4,6})\s*원"""),  // 30000원 (쉼표 없음, 4-6자리)
                                Regex("""가격[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 가격: 30,000원
                                Regex("""입장료[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 입장료: 30,000원
                                Regex("""티켓[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 티켓: 30,000원
                                Regex("""(\d{2,6})원"""),  // 간단한 형식: 30000원 (2-6자리)
                            )
                            
                            // 가장 큰 가격을 선택 (여러 가격이 있을 수 있음)
                            var maxPrice: Int? = null
                            for (pattern in pricePatterns) {
                                val priceMatches = pattern.findAll(searchText)
                                for (priceMatch in priceMatches) {
                                    try {
                                        val priceStr = priceMatch.groupValues[1].replace(",", "").trim()
                                        val parsedPrice = priceStr.toInt()
                                        if (parsedPrice in 5000..500000) { // 합리적인 가격 범위 (5천원~50만원)
                                            if (maxPrice == null || parsedPrice > maxPrice) {
                                                maxPrice = parsedPrice
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // 무시
                                    }
                                }
                            }
                            price = maxPrice
                            if (price != null) {
                                logger.debug("가격 추출 성공 (텍스트): ${price}원 (from: ${searchText.take(150)})")
                            }
                        }
                        
                        logger.info("공연 데이터 추가: title=$title, venue=$venue, dateTime=$dateTime, price=${price ?: "미정"}, startAttr=$finalStartAttr, elementText=${elementText.take(200)}")
                        
                        // 날짜가 제대로 파싱되었는지 확인
                        if (dateTime.year == 2025 && dateTime.monthValue == 12 && dateTime.dayOfMonth == 30) {
                            logger.warn("의심스러운 날짜 감지 (2025-12-30): title=$title, startAttr=$finalStartAttr, dateTime=$dateTime")
                        }
                        
                        // 상세 페이지 URL 추출 시도
                        var detailUrl: String? = null
                        try {
                            val linkElement = element.findElements(By.cssSelector("a")).firstOrNull()
                            if (linkElement != null) {
                                var href = linkElement.getAttribute("href")
                                if (!href.isNullOrBlank()) {
                                    if (!href.startsWith("http")) {
                                        if (href.startsWith("#")) {
                                            detailUrl = "$baseUrl$href"
                                        } else if (href.startsWith("/")) {
                                            detailUrl = "https://chikadol.net$href"
                                        } else {
                                            detailUrl = "$baseUrl/$href"
                                        }
                                    } else {
                                        detailUrl = href
                                    }
                                }
                            }
                            // data-id나 id 속성으로도 시도
                            if (detailUrl.isNullOrBlank()) {
                                val eventId = element.getAttribute("data-id") ?: element.getAttribute("id")
                                if (!eventId.isNullOrBlank() && eventId.matches(Regex("\\d+"))) {
                                    detailUrl = "$baseUrl#calendar/Modify/Form/$eventId"
                                }
                            }
                        } catch (e: Exception) {
                            logger.debug("상세 페이지 URL 추출 실패: ${e.message}")
                        }
                        
                        performances.add(
                            GenbaPerformanceData(
                                title = title.trim(),
                                description = elementText.take(500),
                                venue = venue,
                                performanceDateTime = dateTime,
                                price = price,
                                totalSeats = null,
                                remainingSeats = null,
                                imageUrl = element.findElements(By.cssSelector("img")).firstOrNull()?.getAttribute("src"),
                                detailUrl = detailUrl
                            )
                        )
                    } catch (e: Exception) {
                        logger.debug("이벤트 요소 파싱 실패: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                logger.warn("Selenium 요소 추출 실패: ${e.message}", e)
            }
            
            // FullCalendar API를 통해 모든 이벤트 가져오기 시도
            // clientEvents()는 모든 로드된 이벤트를 반환합니다 (현재 표시된 월뿐만 아니라)
            try {
                logger.info("FullCalendar API를 통한 모든 이벤트 데이터 추출 시도")
                val allEventsJson = jsExecutor.executeScript("""
                    try {
                        // jQuery와 FullCalendar가 로드되었는지 확인
                        if (typeof jQuery === 'undefined') {
                            console.log('jQuery가 로드되지 않음');
                            return null;
                        }
                        
                        var calElement = jQuery('#calendar');
                        if (calElement.length === 0) {
                            console.log('캘린더 요소를 찾을 수 없음');
                            return null;
                        }
                        
                        console.log('캘린더 요소 발견, FullCalendar API 호출 시도');
                        
                        try {
                            // FullCalendar v2/v3 방식 - clientEvents()는 모든 이벤트를 반환
                            var view = calElement.fullCalendar('getView');
                            if (view && view.calendar) {
                                var calendarObj = view.calendar;
                                // clientEvents()는 현재 로드된 모든 이벤트를 반환 (월별이 아닌 전체)
                                var events = calendarObj.clientEvents();
                                console.log('FullCalendar API 전체 이벤트 개수: ' + events.length);
                                
                                if (events && events.length > 0) {
                                       var result = events.map(function(e) {
                                           var startStr = '';
                                           if (e.start) {
                                               if (typeof e.start.format === 'function') {
                                                   startStr = e.start.format('YYYY-MM-DDTHH:mm:ss');
                                               } else if (e.start.toISOString) {
                                                   startStr = e.start.toISOString();
                                               } else {
                                                   startStr = String(e.start);
                                               }
                                           }
                                           
                                           // URL 추출: url, extendedProps.url, 또는 id 기반 URL 생성
                                           var eventUrl = e.url || '';
                                           if (!eventUrl && e.extendedProps && e.extendedProps.url) {
                                               eventUrl = e.extendedProps.url;
                                           }
                                           // id가 있으면 상세 페이지 URL 생성 시도
                                           if (!eventUrl && e.id) {
                                               eventUrl = '#calendar/Modify/Form/' + e.id;
                                           }
                                           
                                           return {
                                               title: e.title || '',
                                               start: startStr,
                                               end: e.end ? (e.end.format ? e.end.format('YYYY-MM-DDTHH:mm:ss') : e.end.toISOString()) : '',
                                               description: e.description || '',
                                               url: eventUrl,
                                               id: e.id || '',
                                               extendedProps: e.extendedProps || {}
                                           };
                                       });
                                    return JSON.stringify(result);
                                }
                            }
                        } catch(e1) {
                            console.error('FullCalendar v2/v3 API 오류:', e1);
                            // FullCalendar v4/v5 방식 시도
                            try {
                                var calElementDom = document.getElementById('calendar');
                                if (calElementDom && calElementDom._fullCalendar) {
                                    var calendarApi = calElementDom._fullCalendar.getApi();
                                    var events = calendarApi.getEvents();
                                    if (events && events.length > 0) {
                                        var result = events.map(function(e) {
                                            return {
                                                title: e.title || '',
                                                start: e.start ? e.start.toISOString() : '',
                                                end: e.end ? e.end.toISOString() : '',
                                                description: e.extendedProps ? (e.extendedProps.description || '') : '',
                                                url: e.url || (e.extendedProps && e.extendedProps.url ? e.extendedProps.url : ''),
                                                id: e.id || ''
                                            };
                                        });
                                        return JSON.stringify(result);
                                    }
                                }
                            } catch(e2) {
                                console.error('FullCalendar v4/v5 API 오류:', e2);
                            }
                        }
                        
                        // 대체 방법: window 객체에서 캘린더 이벤트 찾기
                        if (window.genbaEvents || window.calendarEvents || window.events) {
                            var events = window.genbaEvents || window.calendarEvents || window.events;
                            if (Array.isArray(events) && events.length > 0) {
                                console.log('window 객체에서 이벤트 발견: ' + events.length);
                                return JSON.stringify(events.map(function(e) {
                                    return {
                                        title: e.title || e.name || '',
                                        start: e.start || e.date || e.datetime || '',
                                        description: e.description || e.desc || ''
                                    };
                                }));
                            }
                        }
                    } catch(e) {
                        console.error('전체 오류:', e);
                    }
                    return null;
                """) as String?
                
                if (!allEventsJson.isNullOrBlank() && allEventsJson != "null") {
                    logger.info("FullCalendar API로 이벤트 데이터 추출 성공 (길이: ${allEventsJson.length}): ${allEventsJson.take(1000)}")
                    val events = parseCalendarEvents(allEventsJson)
                    if (events.isNotEmpty()) {
                        performances.addAll(events)
                        logger.info("${events.size}개의 이벤트 파싱 성공 (FullCalendar API)")
                    } else {
                        logger.warn("이벤트 파싱 실패")
                    }
                } else {
                    logger.warn("FullCalendar API에서 이벤트 데이터를 추출하지 못함, DOM 파싱 시도")
                }
                
                // DOM 요소에서 직접 데이터 추출 (FullCalendar API 실패 시 백업 방법)
                logger.info("DOM 요소에서 직접 데이터 추출 시도 (백업 방법)")
                val eventsJson2 = jsExecutor.executeScript("""
                    try {
                        var events = [];
                        // 모든 fc-event 관련 요소 찾기
                        var selectors = [
                            '.fc-event',
                            '.fc-day-grid-event', 
                            '.fc-time-grid-event',
                            '.fc-list-item',
                            '[class*="fc-event"]',
                            '[data-event-id]',
                            '[data-start]'
                        ];
                        
                        var allElements = [];
                        for (var s = 0; s < selectors.length; s++) {
                            var elements = document.querySelectorAll(selectors[s]);
                            for (var i = 0; i < elements.length; i++) {
                                if (allElements.indexOf(elements[i]) === -1) {
                                    allElements.push(elements[i]);
                                }
                            }
                        }
                        
                        console.log('발견한 이벤트 요소 개수: ' + allElements.length);
                        
                        for (var i = 0; i < allElements.length; i++) {
                            var el = allElements[i];
                            
                            // 제목 추출
                            var titleEl = el.querySelector('.fc-title, .fc-event-title, .fc-list-item-title');
                            var title = titleEl ? titleEl.textContent.trim() : el.textContent.trim();
                            
                            // 날짜 추출 (여러 방법 시도)
                            var start = el.getAttribute('data-start') 
                                     || el.getAttribute('start')
                                     || el.getAttribute('data-event-start')
                                     || el.getAttribute('data-date');
                            
                            // 부모 요소에서 날짜 찾기
                            if (!start) {
                                var parent = el.parentElement;
                                while (parent && !start) {
                                    start = parent.getAttribute('data-date') 
                                         || parent.getAttribute('data-start');
                                    parent = parent.parentElement;
                                }
                            }
                            
                            // 전체 텍스트
                            var fullText = el.textContent.trim();
                            
                            if (title && title.length > 2) {
                                events.push({
                                    title: title,
                                    start: start || '',
                                    description: fullText,
                                    fullText: fullText
                                });
                            }
                        }
                        
                        console.log('추출된 이벤트 개수: ' + events.length);
                        return events.length > 0 ? JSON.stringify(events) : null;
                    } catch(e) {
                        console.error('방법2 오류:', e);
                        return null;
                    }
                """) as String?
                
                if (!eventsJson2.isNullOrBlank() && eventsJson2 != "null") {
                    logger.info("DOM 파싱으로 이벤트 데이터 추출 성공 (길이: ${eventsJson2.length}): ${eventsJson2.take(1000)}")
                    val events = parseCalendarEvents(eventsJson2)
                    if (events.isNotEmpty()) {
                        performances.addAll(events)
                        logger.info("${events.size}개의 이벤트 파싱 성공 (DOM 파싱)")
                    } else {
                        logger.warn("DOM 파싱에서 이벤트를 파싱하지 못함")
                    }
                } else {
                    logger.warn("DOM 파싱에서 이벤트 데이터를 추출하지 못함")
                }
            } catch (e: Exception) {
                logger.debug("JavaScript 이벤트 추출 실패: ${e.message}", e)
            }
            
            // 페이지 소스에서 HTML 파싱
            val pageSource = driver.pageSource
            logger.info("페이지 소스 길이: ${pageSource.length} bytes")
            
            val doc: Document = Jsoup.parse(pageSource, baseUrl)
            logger.info("웹페이지 로드 성공: ${doc.title()}")
            
            // 전체 HTML 확인 (디버깅)
            val htmlContent = doc.html()
            logger.info("페이지 HTML 길이: ${htmlContent.length} bytes")
            
            // 스크립트 태그에서 JSON 데이터 찾기 (가장 중요!)
            val scripts = doc.select("script:not([src])")
            logger.info("인라인 script 태그 수: ${scripts.size}")
            
            for ((index, script) in scripts.withIndex()) {
                val scriptContent = script.html() ?: script.data()
                if (scriptContent.isBlank()) continue
                
                // JSON 데이터나 이벤트 데이터가 포함된 스크립트 찾기
                if (scriptContent.contains("event") || scriptContent.contains("genba") || 
                    scriptContent.contains("calendar") || scriptContent.contains("schedule") ||
                    scriptContent.contains("title") && scriptContent.contains("date")) {
                    
                    logger.info("관련 스크립트 #$index 발견 (길이: ${scriptContent.length})")
                    logger.debug("스크립트 일부: ${scriptContent.take(500)}")
                    
                    // JSON 배열 패턴 찾기
                    try {
                        val jsonArrayPattern = Regex("""\[.*?"(?:title|name|event)".*?\]""", RegexOption.DOT_MATCHES_ALL)
                        val arrayMatch = jsonArrayPattern.find(scriptContent)
                        if (arrayMatch != null) {
                            logger.info("JSON 배열 패턴 발견: ${arrayMatch.value.take(300)}")
                            // JSON 파싱 시도 (간단한 경우)
                        }
                    } catch (e: Exception) {
                        logger.debug("JSON 패턴 찾기 실패: ${e.message}")
                    }
                }
            }

            // 모든 텍스트 노드에서 날짜 패턴 찾기
            val bodyText = doc.body().text()
            logger.info("본문 텍스트 길이: ${bodyText.length} chars")
            
            // 날짜 패턴이 포함된 줄 찾기
            val lines = bodyText.split("\n", "\r\n", "\r").map { it.trim() }.filter { it.isNotBlank() }
            logger.info("텍스트 줄 수: ${lines.size}")
            
            var foundCount = 0
            for (line in lines) {
                if (line.length < 10) continue
                
                // 날짜 패턴과 관련 키워드가 있는 줄만 처리
                val hasDate = Regex("""\d{4}[.\-/]\d{1,2}[.\-/]\d{1,2}""").containsMatchIn(line)
                val hasKeyword = line.contains("겐바") || line.contains("공연") || line.contains("라이브") || 
                                line.contains("@") || line.length > 30
                
                if (hasDate && hasKeyword) {
                    try {
                        logger.debug("파싱 시도: ${line.take(150)}")
                        val performance = parseTextContent(line, null)
                        if (performance != null && performance.title.isNotBlank() && 
                            performance.title != "지하돌 공연" && 
                            !performance.title.all { it.isWhitespace() || it.isDigit() }) {
                            logger.info("공연 파싱 성공: ${performance.title} @ ${performance.venue} - ${performance.performanceDateTime}")
                            performances.add(performance)
                            foundCount++
                            if (foundCount >= 50) break // 최대 50개
                        }
                    } catch (e: Exception) {
                        logger.debug("파싱 실패: ${e.message}")
                    }
                }
            }

            // 추가로 모든 div, span 요소에서도 찾기
            if (performances.isEmpty()) {
                logger.info("표준 텍스트 파싱 실패, 모든 요소에서 검색")
                val allDivs = doc.select("div, span, p, li, td")
                for (element in allDivs) {
                    val text = element.text().trim()
                    if (text.length > 20 && Regex("""\d{4}[.\-/]\d{1,2}[.\-/]\d{1,2}""").containsMatchIn(text)) {
                        try {
                            val performance = parseTextContent(text, element)
                            if (performance != null && performances.none { 
                                it.title == performance.title && 
                                it.performanceDateTime == performance.performanceDateTime 
                            }) {
                                logger.info("추가 공연 발견: ${performance.title}")
                                performances.add(performance)
                                foundCount++
                                if (foundCount >= 50) break
                            }
                        } catch (e: Exception) {
                            // 무시
                        }
                    }
                }
            }

            logger.info("현재 월에서 ${performances.size}개 공연 데이터 추출 완료")
            if (performances.isEmpty()) {
                logger.warn("현재 월에서 공연 데이터를 찾지 못했습니다.")
            } else {
                logger.debug("추출된 공연 목록:")
                performances.take(5).forEach { perf ->
                    logger.debug("  - ${perf.title} @ ${perf.venue} - ${perf.performanceDateTime}")
                }
            }
        } catch (e: Exception) {
            logger.error("월 이벤트 수집 중 오류: ${e.message}", e)
        }
        
        return performances
    }

    /**
     * FullCalendar 이벤트 JSON 문자열을 파싱하여 GenbaPerformanceData 리스트로 변환
     */
    private fun parseCalendarEvents(jsonStr: String): List<GenbaPerformanceData> {
        val events = mutableListOf<GenbaPerformanceData>()
        
        if (jsonStr.isBlank()) return events
        
        try {
            // 먼저 실제 JSON 배열인지 확인
            val cleanJson = jsonStr.trim()
            if (!cleanJson.startsWith("[") && !cleanJson.startsWith("{")) {
                logger.debug("JSON 형식이 아닙니다: ${cleanJson.take(100)}")
                return events
            }
            
            // 정규식 기반 파싱 시도
            // 예: [{title: "공연명", start: "2024-01-15T19:00:00", ...}, ...]
            
            // 각 객체 추출 (더 정확한 패턴)
            val objectPattern = Regex("""\{[^{}]*(?:\{[^{}]*\}[^{}]*)*?\}""", RegexOption.DOT_MATCHES_ALL)
            val objects = objectPattern.findAll(cleanJson)
            
            for (objMatch in objects) {
                val objStr = objMatch.value
                
                // title 추출
                val titleMatch = Regex("""['"]title['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val title = titleMatch?.groupValues?.get(1) ?: continue
                
                // start 또는 date 추출 (여러 형식 지원)
                // 우선순위: start > date > datetime
                val startMatch = Regex("""['"]start['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""['"]date['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""['"]datetime['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""start\s*:\s*['"]?([^'",}\]]+)['"]?""").find(objStr)
                    ?: Regex("""date\s*:\s*['"]?([^'",}\]]+)['"]?""").find(objStr)
                    ?: Regex("""datetime\s*:\s*['"]?([^'",}\]]+)['"]?""").find(objStr)
                val dateStr = startMatch?.groupValues?.get(1)?.trim()
                
                if (dateStr.isNullOrBlank() || dateStr.length < 8) {
                    logger.warn("날짜 문자열이 없거나 너무 짧음: dateStr=$dateStr, title=$title, objStr=${objStr.take(200)}")
                    continue
                }
                
                logger.debug("날짜 문자열 추출: dateStr=$dateStr, title=$title")
                
                // description 또는 content 추출
                val descMatch = Regex("""['"]description['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""['"]content['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val description = descMatch?.groupValues?.get(1)
                
                // url 또는 link 추출 (상세 페이지 URL 또는 이미지)
                val urlMatch = Regex("""['"]url['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val extractedUrl = urlMatch?.groupValues?.get(1)
                
                // 이미지 URL 추출
                val imageMatch = Regex("""['"]image['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val imageUrl = imageMatch?.groupValues?.get(1)
                
                // id 추출 (상세 페이지 URL 생성용)
                val idMatch = Regex("""['"]id['"]\s*:\s*['"]?([^'",}\]]+)['"]?""").find(objStr)
                val eventId = idMatch?.groupValues?.get(1)?.trim()
                
                // 상세 페이지 URL 생성 (full URL이 아니면 baseUrl과 조합)
                var detailUrl: String? = extractedUrl
                if (detailUrl.isNullOrBlank() && !eventId.isNullOrBlank()) {
                    // id가 있으면 상세 페이지 URL 생성 (#calendar/Modify/Form/{id})
                    detailUrl = "$baseUrl#calendar/Modify/Form/$eventId"
                    logger.debug("이벤트 ID로 상세 페이지 URL 생성: $detailUrl")
                } else if (!detailUrl.isNullOrBlank() && !detailUrl.startsWith("http")) {
                    // 상대 URL이면 baseUrl과 조합
                    if (detailUrl.startsWith("#")) {
                        detailUrl = "$baseUrl$detailUrl"
                    } else if (detailUrl.startsWith("/")) {
                        detailUrl = "https://chikadol.net$detailUrl"
                    } else {
                        detailUrl = "$baseUrl/$detailUrl"
                    }
                    logger.debug("상대 URL을 절대 URL로 변환: $detailUrl")
                }
                
                if (detailUrl.isNullOrBlank()) {
                    logger.warn("상세 페이지 URL을 생성하지 못함: title=$title, eventId=$eventId, extractedUrl=$extractedUrl")
                }
                
                // 장소 추출 (description에서 @ 뒤 텍스트 찾기)
                var venue = "미정"
                description?.let { desc ->
                    val venueMatch = Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""").find(desc)
                    venue = venueMatch?.groupValues?.get(1)?.trim() ?: "미정"
                }
                
                // 날짜 파싱 (다양한 형식 지원)
                val dateTime = try {
                    when {
                        // ISO 8601 형식: 2024-01-15T19:00:00 또는 2024-01-15T19:00:00Z
                        dateStr.contains("T") -> {
                            // 타임존 정보가 있는지 확인 (Z, +, 또는 날짜 부분 이후의 -)
                            val hasTimeZone = dateStr.endsWith("Z") || dateStr.contains("+") || (dateStr.length > 19 && dateStr[19] == '-' || dateStr[19] == '+')
                            if (hasTimeZone) {
                                // 타임존이 있으면 Instant로 파싱 후 한국 시간으로 변환
                                try {
                                    val instant = java.time.Instant.parse(dateStr)
                                    val zoned = instant.atZone(ZoneId.of("Asia/Seoul")) // 한국 시간으로 명시
                                    val result = zoned.toLocalDateTime()
                                    logger.debug("ISO 8601 (with TZ) 파싱 성공: $dateStr -> $result")
                                    result
                                } catch (e: Exception) {
                                    // 파싱 실패 시 타임존 정보 제거 후 재시도
                                    try {
                                        val cleanStr = dateStr.replace(Regex("[Zz]|([+-]\\d{2}:\\d{2})$"), "").trim()
                                        val result = java.time.LocalDateTime.parse(cleanStr)
                                        logger.debug("ISO 8601 (TZ 제거 후) 파싱 성공: $dateStr -> $result")
                                        result
                                    } catch (e2: Exception) {
                                        logger.warn("ISO 8601 파싱 완전 실패: $dateStr - ${e2.message}")
                                        throw e2
                                    }
                                }
                            } else {
                                // 타임존 정보가 없으면 LocalDateTime으로 직접 파싱
                                try {
                                    val result = java.time.LocalDateTime.parse(dateStr)
                                    logger.debug("ISO 8601 (no TZ) 파싱 성공: $dateStr -> $result")
                                    result
                                } catch (e: Exception) {
                                    logger.warn("ISO 8601 (no TZ) 파싱 실패: $dateStr - ${e.message}")
                                    throw e
                                }
                            }
                        }
                        // YYYY-MM-DD 형식: 2024-01-15
                        dateStr.matches(Regex("""\d{4}-\d{2}-\d{2}"""")) -> {
                            val date = java.time.LocalDate.parse(dateStr)
                            // description에서 시간 정보 찾기
                            val timeMatch = description?.let { desc ->
                                Regex("""(\d{1,2}):(\d{2})""").find(desc)
                            }
                            val result = if (timeMatch != null) {
                                date.atTime(timeMatch.groupValues[1].toInt(), timeMatch.groupValues[2].toInt())
                            } else {
                                date.atTime(19, 0) // 기본 시간
                            }
                            logger.debug("YYYY-MM-DD 파싱 성공: $dateStr -> $result")
                            result
                        }
                        // 다른 형식 시도
                        else -> {
                            val parsed = parseDateTime(dateStr)
                            if (parsed == null) {
                                logger.warn("날짜 파싱 실패 (parseDateTime): $dateStr")
                                throw IllegalArgumentException("날짜 파싱 실패")
                            }
                            logger.debug("parseDateTime 파싱 성공: $dateStr -> $parsed")
                            parsed
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("날짜 파싱 실패: dateStr=$dateStr, title=$title - ${e.message}")
                    val fallbackDate = parseDateTime(dateStr)
                    if (fallbackDate == null) {
                        logger.warn("날짜 파싱 실패, 스킵: dateStr=$dateStr, title=$title")
                        continue
                    }
                    logger.debug("fallback 파싱 성공: $dateStr -> $fallbackDate")
                    fallbackDate
                }
                
                // 과거 날짜는 제외하지 않음 (12월 전체 데이터 수집을 위해)
                // 단, 너무 과거(예: 1년 이상)는 제외
                val oneYearAgo = LocalDateTime.now().minusYears(1)
                if (dateTime.isBefore(oneYearAgo)) {
                    logger.debug("너무 과거 날짜 스킵: $dateTime")
                    continue
                }
                
                // 가격 정보 추출 (description, title, extendedProps에서 원 단위 가격 찾기)
                var price: Int? = null
                
                // extendedProps에서 가격 찾기 시도
                val extendedPropsStr = objStr.substringAfter("\"extendedProps\"").substringBefore("}")
                if (extendedPropsStr.isNotBlank() && extendedPropsStr != objStr) {
                    val priceMatchInProps = Regex("""['"]price['"]\s*:\s*['"]?(\d+)['"]?""").find(extendedPropsStr)
                    if (priceMatchInProps != null) {
                        try {
                            val parsedPrice = priceMatchInProps.groupValues[1].toInt()
                            if (parsedPrice in 5000..500000) {
                                price = parsedPrice
                                logger.debug("가격 추출 성공 (extendedProps): ${price}원")
                            }
                        } catch (e: Exception) {
                            // 무시
                        }
                    }
                }
                
                // description과 title에서 가격 추출
                if (price == null) {
                    val searchText = "${description ?: ""} ${title} ${extendedPropsStr}"
                    
                    // 여러 가격 패턴 시도
                    val pricePatterns = listOf(
                        Regex("""(\d{1,3}(?:,\d{3})*)\s*원"""),  // 30,000원
                        Regex("""₩\s*(\d{1,3}(?:,\d{3})*)"""),  // ₩30,000
                        Regex("""(\d{4,6})\s*원"""),  // 30000원 (쉼표 없음, 4-6자리)
                        Regex("""가격[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 가격: 30,000원
                        Regex("""입장료[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 입장료: 30,000원
                        Regex("""티켓[:\s]*(\d{1,3}(?:,\d{3})*)\s*원"""),  // 티켓: 30,000원
                        Regex("""(\d{2,6})원"""),  // 간단한 형식: 30000원 (2-6자리)
                    )
                    
                    // 가장 큰 가격을 선택 (여러 가격이 있을 수 있음)
                    var maxPrice: Int? = null
                    for (pattern in pricePatterns) {
                        val priceMatches = pattern.findAll(searchText)
                        for (priceMatch in priceMatches) {
                            try {
                                val priceStr = priceMatch.groupValues[1].replace(",", "").trim()
                                val parsedPrice = priceStr.toInt()
                                if (parsedPrice in 5000..500000) { // 합리적인 가격 범위 (5천원~50만원)
                                    if (maxPrice == null || parsedPrice > maxPrice) {
                                        maxPrice = parsedPrice
                                    }
                                }
                            } catch (e: Exception) {
                                // 무시
                            }
                        }
                    }
                    price = maxPrice
                    if (price != null) {
                        logger.debug("가격 추출 성공 (text): ${price}원 (from: ${searchText.take(150)})")
                    } else {
                        logger.debug("가격 추출 실패 (from: ${searchText.take(150)})")
                    }
                }
                
                logger.info("이벤트 파싱 완료: title=$title, dateTime=$dateTime, venue=$venue, price=${price ?: "미정"}")
                
                events.add(
                    GenbaPerformanceData(
                        title = title.trim(),
                        description = description?.trim(),
                        venue = venue.trim(),
                        performanceDateTime = dateTime,
                        price = price,
                        totalSeats = null,
                        remainingSeats = null,
                        imageUrl = imageUrl,
                        detailUrl = detailUrl // 상세 페이지 URL 추가!
                    )
                )
            }
        } catch (e: Exception) {
            logger.warn("캘린더 이벤트 파싱 중 오류: ${e.message}", e)
        }
        
        return events
    }

    private fun parseGenbaElement(element: Element): GenbaPerformanceData? {
        // 실제 HTML 구조에 맞게 수정 필요
        val title = element.select(".title, h3, h4, [data-title]").first()?.text()
            ?: element.ownText().takeIf { it.isNotBlank() }
            ?: return null

        val dateText = element.select(".date, .datetime, [data-date]").first()?.text()
            ?: element.attr("data-date")
        val venue = element.select(".venue, .location, [data-venue]").first()?.text()
            ?: "미정"
        val description = element.select(".description, .detail").first()?.text()

        val performanceDateTime = parseDateTime(dateText) ?: LocalDateTime.now().plusDays(1).withHour(19).withMinute(0)

        return GenbaPerformanceData(
            title = title.trim(),
            description = description?.trim(),
            venue = venue.trim(),
            performanceDateTime = performanceDateTime,
            price = null, // 가격 정보가 없을 수 있음
            totalSeats = null,
            remainingSeats = null,
            imageUrl = element.select("img").first()?.attr("src"),
            detailUrl = null
        )
    }

    private fun parseTextContent(text: String, element: Element?): GenbaPerformanceData? {
        if (text.isBlank() || text.length < 10) return null
        
        // 텍스트에서 패턴 추출 시도
        // 예: "2024.01.15 19:00 지하돌A 공연 @ 홍대 라이브홀"
        // 예: "2024-12-28 지하돌B @ 강남"
        
        val datePattern = Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""")
        val timePattern = Regex("""(\d{1,2}):(\d{2})""")
        
        val dateMatch = datePattern.find(text)
        if (dateMatch == null) return null
        
        val (year, month, day) = dateMatch.destructured
        val timeMatch = timePattern.find(text)
        val hour: String
        val minute: String
        if (timeMatch != null) {
            val (h, m) = timeMatch.destructured
            hour = h
            minute = m
        } else {
            hour = "19"
            minute = "00" // 기본 시간
        }

        val dateTime = try {
            LocalDateTime.of(
                year.toInt(),
                month.toInt(),
                day.toInt(),
                hour.toInt(),
                minute.toInt()
            )
            // 과거 날짜는 제외
        } catch (e: Exception) {
            logger.debug("날짜 파싱 실패: $text - ${e.message}")
            return null
        }
        
        // 과거 날짜는 제외하지 않음 (12월 전체 데이터 수집을 위해)
        // 단, 너무 과거(예: 1년 이상)는 제외
        val oneYearAgo = LocalDateTime.now().minusYears(1)
        if (dateTime.isBefore(oneYearAgo)) {
            logger.debug("너무 과거 날짜 스킵 (parseTextContent): $dateTime")
            return null
        }

        // 장소 추출 (보통 @ 뒤에 나오거나 특정 패턴)
        val venuePatterns = listOf(
            Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""" ),  // @ 홍대 라이브홀
            Regex("""장소[:\s]+([가-힣a-zA-Z0-9\s]+)""" ),  // 장소: 홍대
            Regex("""([가-힣]+(?:홀|스튜디오|클럽|공연장))""" )  // 홍대라이브홀
        )
        
        var venue = "미정"
        for (pattern in venuePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                venue = match.groupValues.getOrNull(1)?.trim() ?: "미정"
                if (venue.isNotBlank() && venue != "미정") break
            }
        }

        // 제목 추출 - 날짜 앞뒤 텍스트에서 추출
        val beforeDate = text.substring(0, dateMatch.range.first).trim()
        val afterDate = text.substring(dateMatch.range.last + 1).trim()
        
        var title = "지하돌 공연"
        // 날짜 앞의 텍스트에서 제목 찾기
        if (beforeDate.isNotBlank() && beforeDate.length > 2) {
            title = beforeDate.take(100)
        } else if (afterDate.isNotBlank()) {
            // 날짜 뒤에서 @ 전까지를 제목으로
            val titleMatch = Regex("""([^@]+?)(?:@|$)""").find(afterDate)
            if (titleMatch != null) {
                title = titleMatch.groupValues[1].trim().take(100)
            } else {
                title = afterDate.split("@").first().trim().take(100)
            }
        }
        
        // 너무 짧거나 의미없는 제목은 스킵
        if (title.length < 2 || title == "지하돌 공연" && text.length < 30) {
            return null
        }

        return GenbaPerformanceData(
            title = title,
            description = text.trim().take(500),
            venue = venue,
            performanceDateTime = dateTime,
            price = null,
            totalSeats = null,
            remainingSeats = null,
            imageUrl = null,
            detailUrl = null
        )
    }

    private fun parseDateTime(dateText: String?): LocalDateTime? {
        if (dateText == null || dateText.isBlank()) {
            return null // 기본값 반환하지 않고 null 반환 (스킵하도록)
        }

        val cleanText = dateText.trim()

        // 패턴들 시도
        val patterns = listOf(
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.KOREAN),
            DateTimeFormatter.ofPattern("MM.dd HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.KOREAN),
        )

        for (pattern in patterns) {
            try {
                if (pattern.toString().contains("HH:mm")) {
                    val parsed = LocalDateTime.parse(cleanText, pattern)
                    // 연도가 없으면 올해로 설정
                    if (parsed.year == 2000) {
                        return parsed.withYear(LocalDateTime.now().year)
                    }
                    return parsed
                } else {
                    val parsed = LocalDate.parse(cleanText, pattern)
                    val dateTime = parsed.atTime(19, 0)
                    // 연도가 없으면 올해로 설정
                    if (dateTime.year == 2000) {
                        return dateTime.withYear(LocalDateTime.now().year)
                    }
                    return dateTime
                }
            } catch (e: DateTimeParseException) {
                // 다음 패턴 시도
            }
        }

        // 정규식으로 파싱 시도
        val datePattern = Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""")
        val timePattern = Regex("""(\d{1,2}):(\d{2})""")
        
        val dateMatch = datePattern.find(cleanText)
        val timeMatch = timePattern.find(cleanText)
        
        if (dateMatch != null) {
            try {
                val (year, month, day) = dateMatch.destructured
                val hour: String
                val minute: String
                if (timeMatch != null) {
                    val (h, m) = timeMatch.destructured
                    hour = h
                    minute = m
                } else {
                    hour = "19"
                    minute = "00"
                }
                return LocalDateTime.of(
                    year.toInt(),
                    month.toInt(),
                    day.toInt(),
                    hour.toInt(),
                    minute.toInt()
                )
            } catch (e: Exception) {
                logger.warn("정규식 날짜 파싱 실패: $cleanText", e)
            }
        }

        // 파싱 실패 시 null 반환
        logger.debug("날짜 파싱 실패: $cleanText")
        return null
    }

        private fun extractPerformersFromDetail(detailUrl: String): List<String> {
            return try {
                val doc = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get()

                val candidates = mutableListOf<String>()

                // 시간표 패턴에서 이름 추출 (예: 19:20-19:40 유포아이)
                val timeNameRegex = Regex("""\b\d{1,2}:\d{2}\s*[–-]\s*\d{1,2}:\d{2}\s+([^\n\r]{1,60}?)\b""")

                // 1) 메타 태그 (og:title / og:description / description)
                listOf(
                    doc.selectFirst("meta[property=og:description]")?.attr("content"),
                    doc.selectFirst("meta[property=og:title]")?.attr("content"),
                    doc.selectFirst("meta[name=description]")?.attr("content")
                ).forEach { meta ->
                    if (!meta.isNullOrBlank()) candidates.add(meta)
                }

                // 2) 본문 텍스트에서 패턴 검색
                val bodyText = doc.body().text()
                val patterns = listOf(
                    Regex("""출연(?:진|자)?\s*[:：]?\s*([^\n]+)""", RegexOption.IGNORE_CASE),
                    Regex("""라인업\s*[:：]?\s*([^\n]+)""", RegexOption.IGNORE_CASE),
                    Regex("""LINE\s?UP\s*[:：]?\s*([^\n]+)""", RegexOption.IGNORE_CASE),
                    Regex("""出演\s*[:：]?\s*([^\n]+)""", RegexOption.IGNORE_CASE)
                )
                for (p in patterns) {
                    val m = p.find(bodyText)
                    if (m != null) {
                        candidates.add(m.groupValues[1])
                        break
                    }
                }

                // 3) 리스트 요소(li) 우선 스캔
                val listBlocks = doc.select("li").mapNotNull { it.text()?.trim() }.filter { it.length in 2..200 }
                candidates.addAll(listBlocks)

                // 3-1) 테이블/타임테이블에서 시간+이름 추출
                val tableRows = doc.select("table tr").mapNotNull { it.text()?.trim() }.filter { it.isNotEmpty() }
                val timeFromTable = tableRows.flatMap { row ->
                    timeNameRegex.findAll(row).map { it.groupValues[1] }.toList()
                }
                candidates.addAll(timeFromTable)

                // 4) 라벨/키워드가 붙은 DOM 요소 탐색
                val keywordElems = doc.select("*:matchesOwn((?i)출연|출연진|라인업|line up|出演)")
                for (elem in keywordElems) {
                    val text = elem.parent()?.text() ?: elem.text()
                    if (!text.isNullOrBlank()) candidates.add(text)
                }

                // 5) fallback: 텍스트 블록에서 구분자 또는 2개 이상의 토큰이 있는 첫 문장
                if (candidates.isEmpty()) {
                    val blocks = doc.select("p, div, span")
                        .mapNotNull { it.text()?.trim() }
                        .filter { it.isNotEmpty() && it.length in 5..200 }
                    for (b in blocks) {
                        val tokens = b.split(",", "/", "|", "·", "・", "-", "–")
                            .flatMap { it.split(" ") }
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && it.length in 1..50 }
                        if (tokens.size >= 2) {
                            candidates.add(tokens.joinToString(","))
                            break
                        }
                    }
                }

                // 6) 본문 전체에서 시간+이름 패턴 추가 스캔
                val timeFromBody = timeNameRegex.findAll(bodyText).map { it.groupValues[1] }.toList()
                candidates.addAll(timeFromBody)

                if (candidates.isEmpty()) return emptyList()

                val performers = candidates.flatMap { line ->
                    line.split(",", "/", "|", "·", "・", "-", "–")
                        .flatMap { it.split(" ") }
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.length in 1..50 }
                }.distinct()

                if (performers.isEmpty()) {
                    logger.debug("출연진 파싱 결과 없음: $detailUrl, body(200자)=${bodyText.take(200)}")
                }
                performers
            } catch (e: Exception) {
                logger.debug("출연진 추출 실패: $detailUrl, ${e.message}")
                emptyList()
            }
        }
        private data class GenbaPerformanceData(
            val title: String,
            val description: String?,
            val venue: String,
            val performanceDateTime: LocalDateTime,
            val price: Int?,
            val totalSeats: Int?,
            val remainingSeats: Int?,
            val imageUrl: String?,
            val detailUrl: String? = null, // 상세 페이지 URL
            val performers: List<String> = emptyList() // 출연진
        )

        private fun resolveIdolId(performers: List<String>, title: String): UUID? {
            // 1순위: 출연진 목록에서 첫 번째 이름
            val candidate = performers.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
            // 2순위: 제목 선두 토큰
            val fallback = title
                .split("-", "–", "|", "@")
                .firstOrNull()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            val name = candidate ?: fallback ?: return null

            val existing = idolRepository.findByNameIgnoreCase(name)
            if (existing != null) return existing.id

            return try {
                idolRepository.save(IdolEntity(name = name)).id
            } catch (e: Exception) {
                logger.warn("아이돌 저장 실패(무시): $name, ${e.message}")
                null
            }
        }
}


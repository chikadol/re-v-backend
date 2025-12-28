package com.rev.app.api.service.ticket

import com.rev.app.domain.ticket.entity.PerformanceEntity
import com.rev.app.domain.ticket.entity.PerformanceStatus
import com.rev.app.domain.ticket.repo.PerformanceRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Service
class GenbaCrawlerService(
    private val performanceRepository: PerformanceRepository,
    private val performanceService: PerformanceService
) {
    private val logger = LoggerFactory.getLogger(GenbaCrawlerService::class.java)
    private val baseUrl = "https://chikadol.net/genba"

    // 매시간 실행 (크롤링 빈도 조절 가능)
    // cron 표현식: 초 분 시 일 월 요일
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각
    // @Scheduled(fixedRate = 3600000) // 1시간마다 (밀리초)
    @Transactional
    fun crawlGenbaSchedules() {
        logger.info("겐바 일정 크롤링 시작")
        try {
            val performances = fetchGenbaPerformances()
            logger.info("크롤링으로 추출된 공연 수: ${performances.size}")
            
            if (performances.isEmpty()) {
                logger.warn("크롤링 결과가 비어있습니다. 웹사이트 구조 확인이 필요합니다.")
                return
            }

            var created = 0
            var updated = 0
            var skipped = 0

            for (performanceData in performances) {
                try {
                    // 중복 체크: 제목과 일시로 비교
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
                                price = performanceData.price ?: 30000, // 기본 가격 3만원
                                totalSeats = performanceData.totalSeats ?: 100, // 기본 좌석 100석
                                imageUrl = performanceData.imageUrl
                            )
                        )
                        created++
                        logger.info("새 공연 추가: ${performanceData.title} @ ${performanceData.venue} - ${performanceData.performanceDateTime}")
                    } else {
                        // 기존 공연 업데이트 (좌석 수 등이 변경될 수 있음)
                        if (performanceData.remainingSeats != null && existing.remainingSeats != performanceData.remainingSeats) {
                            existing.remainingSeats = performanceData.remainingSeats
                            performanceRepository.save(existing)
                            updated++
                            logger.info("공연 업데이트: ${performanceData.title}")
                        } else {
                            skipped++
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

    private fun fetchGenbaPerformances(): List<GenbaPerformanceData> {
        val performances = mutableListOf<GenbaPerformanceData>()

        try {
            logger.info("크롤링 시작: $baseUrl")
            val doc: Document = Jsoup.connect(baseUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://chikadol.net")
                .timeout(15000)
                .get()

            logger.info("웹페이지 로드 성공: ${doc.title()}")
            logger.debug("페이지 HTML 길이: ${doc.html().length} bytes")

            // HTML 구조 디버깅: 주요 요소 찾기
            val bodyText = doc.body().text()
            logger.info("페이지 본문 텍스트 길이: ${bodyText.length} chars")
            logger.debug("페이지 본문 일부: ${bodyText.take(500)}")

            // 캘린더나 이벤트 관련 요소 찾기 시도
            val allElements = doc.select("*")
            logger.info("전체 HTML 요소 수: ${allElements.size}")
            
            // 클래스 이름에서 힌트 찾기
            val classNames = allElements.mapNotNull { it.className().takeIf { cn -> cn.isNotBlank() } }.distinct().take(20)
            logger.info("발견된 주요 클래스명: ${classNames.joinToString(", ")}")

            // 다양한 선택자로 이벤트 요소 찾기
            val eventSelectors = listOf(
                ".event", ".schedule-item", ".genba-item", 
                "[data-genba]", ".performance-item", ".schedule",
                ".fc-event-title", ".fc-event-time", ".calendar-event",
                ".fc-event", ".fc-day-event", "[data-event]",
                ".schedule-list", ".event-list", ".genba-list"
            )
            
            var foundElements = false
            
            for (selector in eventSelectors) {
                val elements = doc.select(selector)
                if (elements.isNotEmpty()) {
                    logger.info("선택자 '$selector'로 ${elements.size}개 요소 발견")
                    for (element in elements.take(5)) { // 처음 5개만 로그
                        logger.debug("요소 텍스트: ${element.text().take(100)}")
                    }
                    for (element in elements) {
                        try {
                            val performance = parseGenbaElement(element)
                            if (performance != null) {
                                logger.info("공연 파싱 성공: ${performance.title} - ${performance.venue}")
                                performances.add(performance)
                                foundElements = true
                            }
                        } catch (e: Exception) {
                            logger.warn("요소 파싱 실패 (선택자: $selector): ${e.message}", e)
                        }
                    }
                    if (foundElements) break
                }
            }

            // 테이블이나 리스트에서 파싱 시도
            if (!foundElements) {
                logger.info("이벤트 요소를 찾지 못해 테이블/리스트에서 파싱 시도")
                val tableRows = doc.select("table tr, .list-item, li, .item, div[class*='event'], div[class*='schedule']")
                logger.info("테이블/리스트 행 수: ${tableRows.size}")
                var processedCount = 0
                for (row in tableRows) {
                    val text = row.text().trim()
                    if (text.length > 10 && (text.contains("겐바") || text.contains("공연") || 
                        text.contains("라이브") || text.matches(Regex(".*\\d{4}[.\\-/]\\d{1,2}[.\\-/]\\d{1,2}.*")))) {
                        try {
                            logger.debug("파싱 시도 중: ${text.take(100)}")
                            val performance = parseTextContent(text, row)
                            if (performance != null) {
                                logger.info("텍스트에서 공연 파싱 성공: ${performance.title}")
                                performances.add(performance)
                                foundElements = true
                                processedCount++
                                if (processedCount >= 20) break // 최대 20개만 처리
                            }
                        } catch (e: Exception) {
                            logger.debug("텍스트 파싱 실패: ${text.take(100)} - ${e.message}")
                        }
                    }
                }
            }

            logger.info("총 ${performances.size}개 공연 데이터 추출 완료")
            if (performances.isEmpty()) {
                logger.warn("공연 데이터를 찾지 못했습니다. 웹사이트 구조가 변경되었을 수 있습니다.")
            }

        } catch (e: Exception) {
            logger.error("웹페이지 로드 실패: ${e.message}", e)
        }

        return performances.distinctBy { "${it.title}_${it.performanceDateTime}" }
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

        val performanceDateTime = parseDateTime(dateText)

        return GenbaPerformanceData(
            title = title.trim(),
            description = description?.trim(),
            venue = venue.trim(),
            performanceDateTime = performanceDateTime,
            price = null, // 가격 정보가 없을 수 있음
            totalSeats = null,
            remainingSeats = null,
            imageUrl = element.select("img").first()?.attr("src")
        )
    }

    private fun parseTextContent(text: String, element: Element?): GenbaPerformanceData? {
        if (text.isBlank()) return null
        
        // 텍스트에서 패턴 추출 시도
        // 예: "2024.01.15 19:00 지하돌A 공연 @ 홍대 라이브홀"
        
        val datePattern = Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""")
        val timePattern = Regex("""(\d{1,2}):(\d{2})""")
        
        val dateMatch = datePattern.find(text)
        val timeMatch = timePattern.find(text)
        
        if (dateMatch == null) return null
        
        val (year, month, day) = dateMatch.destructured
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
        } catch (e: Exception) {
            logger.warn("날짜 파싱 실패: $text", e)
            return null
        }

        // 장소 추출 (보통 @ 뒤에 나옴)
        val venueMatch = Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""").find(text)
        val venue = venueMatch?.groupValues?.get(1)?.trim() ?: "미정"

        // 제목 추출 (날짜/시간 전의 텍스트)
        val titleParts = text.split(Regex("""\d{4}[.\-/]\d{1,2}[.\-/]\d{1,2}"""))
        val title = if (titleParts.isNotEmpty()) {
            titleParts[0].trim().takeIf { it.isNotBlank() } ?: "지하돌 공연"
        } else {
            "지하돌 공연"
        }

        return GenbaPerformanceData(
            title = title,
            description = text.trim(),
            venue = venue,
            performanceDateTime = dateTime,
            price = null,
            totalSeats = null,
            remainingSeats = null,
            imageUrl = null
        )
    }

    private fun parseDateTime(dateText: String?): LocalDateTime {
        if (dateText == null || dateText.isBlank()) {
            return LocalDateTime.now().plusDays(1).withHour(19).withMinute(0)
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

        // 파싱 실패 시 기본값
        logger.warn("날짜 파싱 실패, 기본값 사용: $cleanText")
        return LocalDateTime.now().plusDays(1).withHour(19).withMinute(0)
    }

    private data class GenbaPerformanceData(
        val title: String,
        val description: String?,
        val venue: String,
        val performanceDateTime: LocalDateTime,
        val price: Int?,
        val totalSeats: Int?,
        val remainingSeats: Int?,
        val imageUrl: String?
    )
}


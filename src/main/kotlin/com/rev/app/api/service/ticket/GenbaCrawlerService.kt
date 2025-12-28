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
import java.time.ZoneId
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
                .timeout(20000)
                .followRedirects(true)
                .get()

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

            logger.info("총 ${performances.size}개 공연 데이터 추출 완료")
            if (performances.isEmpty()) {
                logger.warn("""
                    공연 데이터를 찾지 못했습니다.
                    HTML 일부: ${doc.body().text().take(2000)}
                    스크립트 개수: ${doc.select("script").size}
                """.trimIndent())
            } else {
                performances.forEach { perf ->
                    logger.info("추출된 공연: ${perf.title} @ ${perf.venue} - ${perf.performanceDateTime}")
                }
            }

        } catch (e: Exception) {
            logger.error("웹페이지 로드 실패: ${e.message}", e)
        }

        return performances.distinctBy { "${it.title}_${it.performanceDateTime}" }
    }

    /**
     * FullCalendar 이벤트 JSON 문자열을 파싱하여 GenbaPerformanceData 리스트로 변환
     */
    private fun parseCalendarEvents(jsonArrayStr: String): List<GenbaPerformanceData> {
        val events = mutableListOf<GenbaPerformanceData>()
        
        try {
            // 간단한 JSON 파싱 (정규식 기반)
            // 예: [{title: "공연명", start: "2024-01-15T19:00:00", ...}, ...]
            
            // 각 객체 추출
            val objectPattern = Regex("""\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}""", RegexOption.DOT_MATCHES_ALL)
            val objects = objectPattern.findAll(jsonArrayStr)
            
            for (objMatch in objects) {
                val objStr = objMatch.value
                
                // title 추출
                val titleMatch = Regex("""['"]title['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val title = titleMatch?.groupValues?.get(1) ?: continue
                
                // start 또는 date 추출
                val startMatch = Regex("""['"]start['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""['"]date['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val dateStr = startMatch?.groupValues?.get(1) ?: continue
                
                // description 또는 content 추출
                val descMatch = Regex("""['"]description['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""['"]content['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val description = descMatch?.groupValues?.get(1)
                
                // url 또는 link 추출 (이미지 등)
                val urlMatch = Regex("""['"]url['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                    ?: Regex("""['"]image['"]\s*:\s*['"]([^'"]+)['"]""").find(objStr)
                val imageUrl = urlMatch?.groupValues?.get(1)
                
                // 장소 추출 (description에서 @ 뒤 텍스트 찾기)
                var venue = "미정"
                description?.let { desc ->
                    val venueMatch = Regex("""@\s*([가-힣a-zA-Z0-9\s]+)""").find(desc)
                    venue = venueMatch?.groupValues?.get(1)?.trim() ?: "미정"
                }
                
                // 날짜 파싱
                val dateTime = try {
                    // ISO 8601 형식: 2024-01-15T19:00:00 또는 2024-01-15
                    if (dateStr.contains("T")) {
                        java.time.Instant.parse(dateStr).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    } else {
                        java.time.LocalDate.parse(dateStr).atTime(19, 0)
                    }
                } catch (e: Exception) {
                    logger.debug("날짜 파싱 실패: $dateStr - ${e.message}")
                    continue
                }
                
                // 과거 날짜는 제외
                if (dateTime.isBefore(LocalDateTime.now())) {
                    continue
                }
                
                events.add(
                    GenbaPerformanceData(
                        title = title.trim(),
                        description = description?.trim(),
                        venue = venue.trim(),
                        performanceDateTime = dateTime,
                        price = null,
                        totalSeats = null,
                        remainingSeats = null,
                        imageUrl = imageUrl
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
        
        // 과거 날짜는 제외
        if (dateTime.isBefore(LocalDateTime.now())) {
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


package com.rev.app.api.controller

import com.rev.app.api.service.ticket.AgitoCrawlerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/agito-crawler")
class AgitoCrawlerController(
    private val agitoCrawlerService: AgitoCrawlerService
) {
    @PostMapping("/crawl")
    fun triggerCrawl(
        @RequestParam(name = "clear", required = false, defaultValue = "false") clear: Boolean
    ): ResponseEntity<Map<String, String>> {
        // 수동으로 크롤링 실행 (관리자용)
        try {
            agitoCrawlerService.crawlAgitoSchedules(clearExisting = clear)
            return ResponseEntity.ok(mapOf("message" to "Agito 크롤링이 완료되었습니다. 로그를 확인하세요."))
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(mapOf("error" to "Agito 크롤링 실패: ${e.message}"))
        }
    }

    @GetMapping("/crawl")
    fun triggerCrawlGet(
        @RequestParam(name = "clear", required = false, defaultValue = "false") clear: Boolean
    ): ResponseEntity<Map<String, String>> {
        // GET 요청도 허용 (테스트용)
        return triggerCrawl(clear)
    }
}


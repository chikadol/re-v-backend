package com.rev.app.api.controller

import com.rev.app.api.service.ticket.GenbaCrawlerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/genba-crawler")
class GenbaCrawlerController(
    private val genbaCrawlerService: GenbaCrawlerService
) {
    @PostMapping("/crawl")
    fun triggerCrawl(): ResponseEntity<Map<String, String>> {
        // 수동으로 크롤링 실행 (관리자용)
        try {
            genbaCrawlerService.crawlGenbaSchedules()
            return ResponseEntity.ok(mapOf("message" to "크롤링이 완료되었습니다. 로그를 확인하세요."))
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(mapOf("error" to "크롤링 실패: ${e.message}"))
        }
    }

    @GetMapping("/crawl")
    fun triggerCrawlGet(): ResponseEntity<Map<String, String>> {
        // GET 요청도 허용 (테스트용)
        return triggerCrawl()
    }
}


package com.rev.app.api.controller

import com.rev.app.api.service.ticket.AgitoCrawlerService
import com.rev.app.api.service.ticket.GenbaCrawlerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/agito-crawler")
class AgitoCrawlerController(
    private val agitoCrawlerService: AgitoCrawlerService,
    private val genbaCrawlerService: GenbaCrawlerService
) {
    @RequestMapping(value = ["/crawl"], method = [RequestMethod.GET, RequestMethod.POST], produces = ["application/json;charset=UTF-8"])
    fun triggerCrawl(
        @RequestParam(name = "clear", required = false, defaultValue = "false") clear: Boolean
    ): ResponseEntity<Map<String, String>> {
        // 수동으로 크롤링 실행 (관리자용)
        // azito.kr은 앱 다운로드 페이지이므로 Genba 크롤러 사용
        try {
            genbaCrawlerService.crawlGenbaSchedules(clearExisting = clear)
            return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("message" to "크롤링이 완료되었습니다. (Genba 크롤러 사용 - azito.kr은 앱 전용 페이지입니다)"))
        } catch (e: Exception) {
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("error" to "크롤링 실패: ${e.message}"))
        }
    }
    
    @RequestMapping(value = ["/crawl-agito"], method = [RequestMethod.GET, RequestMethod.POST], produces = ["application/json;charset=UTF-8"])
    fun triggerAgitoCrawl(
        @RequestParam(name = "clear", required = false, defaultValue = "false") clear: Boolean
    ): ResponseEntity<Map<String, String>> {
        // Agito 크롤러 직접 실행 (테스트용) - GET/POST 모두 지원
        try {
            agitoCrawlerService.crawlAgitoSchedules(clearExisting = clear)
            return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("message" to "Agito 크롤링이 완료되었습니다. 로그를 확인하세요."))
        } catch (e: Exception) {
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("error" to "Agito 크롤링 실패: ${e.message}"))
        }
    }
}


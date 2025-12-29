package com.rev.app.api.controller

import com.rev.app.api.service.ticket.AgitoCrawlerService
import com.rev.app.api.service.ticket.GenbaCrawlerService
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/admin/agito-crawler")
class AgitoCrawlerController(
    private val agitoCrawlerService: AgitoCrawlerService,
    private val genbaCrawlerService: GenbaCrawlerService
) {
    @RequestMapping(value = ["/crawl"], method = [RequestMethod.GET, RequestMethod.POST], produces = ["application/json;charset=UTF-8"])
    fun triggerCrawl(
        @RequestParam(name = "clear", required = false, defaultValue = "false") clear: Boolean,
        @RequestParam(name = "fast", required = false, defaultValue = "true") fast: Boolean
    ): ResponseEntity<Map<String, String>> {
        // 수동으로 크롤링 실행 (관리자용)
        // azito.kr은 앱 다운로드 페이지이므로 Genba 크롤러 사용
        // 비동기로 실행하여 즉시 응답 반환
        try {
            // 비동기로 크롤링 시작
            CompletableFuture.runAsync {
                try {
                    genbaCrawlerService.crawlGenbaSchedules(clearExisting = clear, fast = fast)
                } catch (e: Exception) {
                    println("크롤링 실행 중 오류: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("message" to "크롤링이 시작되었습니다. 잠시 후 공연 목록이 업데이트됩니다."))
        } catch (e: Exception) {
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("error" to "크롤링 시작 실패: ${e.message}"))
        }
    }
    
    @RequestMapping(value = ["/crawl-agito"], method = [RequestMethod.GET, RequestMethod.POST], produces = ["application/json;charset=UTF-8"])
    fun triggerAgitoCrawl(
        @RequestParam(name = "clear", required = false, defaultValue = "false") clear: Boolean
    ): ResponseEntity<Map<String, String>> {
        // Agito 크롤러 직접 실행 (테스트용) - GET/POST 모두 지원
        // 비동기로 실행하여 즉시 응답 반환
        try {
            // 비동기로 크롤링 시작
            CompletableFuture.runAsync {
                try {
                    agitoCrawlerService.crawlAgitoSchedules(clearExisting = clear)
                } catch (e: Exception) {
                    println("Agito 크롤링 실행 중 오류: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("message" to "Agito 크롤링이 시작되었습니다. 로그를 확인하세요."))
        } catch (e: Exception) {
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(mapOf("error" to "Agito 크롤링 시작 실패: ${e.message}"))
        }
    }
}


package com.rev.app.api.controller

import com.rev.app.api.service.idol.IdolService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val idolService: IdolService
) {
    @DeleteMapping("/idols/all")
    fun deleteAllIdols(): ResponseEntity<Map<String, String>> {
        idolService.deleteAll()
        return ResponseEntity.ok(mapOf("message" to "모든 아이돌 데이터가 삭제되었습니다."))
    }
}


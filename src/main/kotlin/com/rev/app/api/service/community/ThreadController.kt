// src/main/kotlin/com/rev/app/api/service/community/ThreadController.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        @RequestParam(required = false, name = "sort") sorts: List<String>?
    ): List<ThreadRes> {
        val allowed = setOf("createdAt","updatedAt")
        sorts?.forEach { s ->
            val key = s.substringBefore(',').trim()
            if (key.isNotBlank() && key !in allowed) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid sort key: $key")
            }
        }
        return threadService.listPublic(boardId)
    }

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @PathVariable boardId: UUID,
        @RequestBody req: CreateThreadReq,   // 이미 CreateThreadReq 로 정리하셨던 버전
        @AuthenticationPrincipal principal: com.rev.app.api.security.JwtPrincipal
    ): ThreadRes {
        return threadService.createInBoard(principal.userId, boardId, req)
    }
}

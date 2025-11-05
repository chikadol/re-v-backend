package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    private val allowedSort = setOf("createdAt", "updatedAt")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        @PageableDefault(size = 10) pageable: Pageable
    ): Page<ThreadRes> {
        // 간단한 sort 화이트리스트
        val invalid = pageable.sort
            .filter { it.property !in allowedSort }
            .any()
        if (invalid) throw IllegalArgumentException("Unsupported sort key")

        return threadService.listPublic(boardId, pageable)
    }

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable boardId: UUID,
        @RequestBody req: CreateThreadReq
    ): ResponseEntity<ThreadRes> =
        ResponseEntity.ok(threadService.createInBoard(me.userId!!, boardId, req))
}

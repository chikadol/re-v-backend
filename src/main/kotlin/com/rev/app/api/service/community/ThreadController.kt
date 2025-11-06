package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    private val allowedSort = setOf("createdAt","updatedAt","title")

    private fun validateSort(pageable: Pageable) {
        pageable.sort.forEach { order ->
            require(allowedSort.contains(order.property)) { "invalid sort key: ${order.property}" }
        }
    }

    // GET /api/threads/{boardId}/threads?page=&size=&sort=createdAt,desc
    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<ThreadRes>> {
        validateSort(pageable)
        return ResponseEntity.ok(threadService.listPublic(boardId, pageable))
    }

    // POST /api/threads/boards/{boardId}
    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable boardId: UUID,
        @RequestBody req: CreateThreadReq
    ): ResponseEntity<ThreadRes> =
        ResponseEntity.ok(threadService.createInBoard(requireNotNull(me.userId), boardId, req))
}

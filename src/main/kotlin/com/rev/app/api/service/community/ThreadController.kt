package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    // 허용 정렬키 (테스트에서 사용하는 createdAt 포함)
    private val ALLOWED_SORT_KEYS = setOf("createdAt", "updatedAt", "title")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        pageable: Pageable
    ): Page<ThreadRes> {
        // sort 파라미터가 온 경우만 검증
        if (pageable.sort.isSorted) {
            pageable.sort.forEach { order ->
                val key = order.property
                if (key !in ALLOWED_SORT_KEYS) {
                    throw IllegalArgumentException("Invalid sort key: $key")
                }
            }
        }
        return threadService.listPublic(boardId, pageable)
    }

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @PathVariable boardId: UUID,
        @RequestBody req: com.rev.app.api.service.community.dto.CreateThreadReq,
        @AuthenticationPrincipal principal: com.rev.app.api.security.JwtPrincipal
    ): ThreadRes {
        return threadService.createInBoard(principal.userId!!, boardId, req)
    }
}

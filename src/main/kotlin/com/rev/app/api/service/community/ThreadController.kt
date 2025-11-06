package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val service: ThreadService
) {
    private val allowSort = setOf("createdAt", "updatedAt", "title")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        pageable: Pageable
    ): Page<ThreadRes> {
        // 허용되지 않은 정렬키면 400
        pageable.sort.forEach {
            require(it.property in allowSort) { "Unsupported sort: ${it.property}" }
        }
        return service.listPublic(boardId, pageable)
    }

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @PathVariable boardId: UUID,
        @AuthenticationPrincipal me: JwtPrincipal,
        @RequestBody req: CreateThreadReq
    ): ThreadRes {
        val userId = requireNotNull(me.userId)
        return service.createInBoard(userId, boardId, req)
    }
}

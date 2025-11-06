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
    private val threadService: ThreadService
) {
    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        pageable: Pageable
    ): Page<ThreadRes> = threadService.listPublic(boardId, pageable)

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable boardId: UUID,
        @RequestBody req: CreateThreadReq
    ): ThreadRes = threadService.createInBoard(requireNotNull(me.userId), boardId, req)
}

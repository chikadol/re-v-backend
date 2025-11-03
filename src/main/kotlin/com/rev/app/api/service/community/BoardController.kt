package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.ThreadRes
import io.swagger.v3.oas.annotations.Parameter
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val threadService: ThreadService
) {
    @GetMapping("/{boardId}")
    fun getBoard(@PathVariable boardId: Long): BoardRes =
        boardService.get(boardId)

    @GetMapping("/{boardId}/threads")
    fun listThreads(
        @PathVariable boardId: Long,
        @ParameterObject pageable: Pageable
    ): Page<ThreadRes> =
        threadService.listPublic(boardId, pageable)

    @PostMapping("/{boardId}/threads")
    fun createThreadInBoard(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable boardId: Long,
        @RequestBody req: CreateThreadReq
    ): ThreadRes {
        val me = requireNotNull(principal.userId) { "No user id in principal" }
        return threadService.createInBoard(me, boardId, req) // ← 이름 맞춰서 호출
    }
}

package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadCreateReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.BoardRes
import org.springframework.data.domain.Page
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val threadService: ThreadService
) {
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): BoardRes =
        boardService.get(id)

    @GetMapping
    fun list(): List<BoardRes> = boardService.list()

    @GetMapping("/{boardId}/threads")
    fun listPublic(@PathVariable boardId: UUID): Page<ThreadRes> =
        threadService.listPublic(
            boardId,
            pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        )

    @PostMapping("/{boardId}/threads")
    fun createInBoard(
        @PathVariable boardId: UUID,
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody req: ThreadCreateReq
    ): ThreadRes? =
        threadService.createInBoard(boardId, principal.userId, req)
}

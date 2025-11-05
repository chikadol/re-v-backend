package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.CreateThreadReq
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val threadService: ThreadService
) {
    @GetMapping
    fun listBoards(): List<BoardRes> = boardService.list()

    @GetMapping("/{id}")
    fun getBoard(@PathVariable id: UUID): BoardRes = boardService.get(id)

    // 보드에 쓰레드 생성 (기존 ThreadCreateReq → CreateThreadReq 로 통일)
    @PostMapping("/{boardId}/threads")
    fun createThreadInBoard(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable boardId: UUID,
        @RequestBody req: CreateThreadReq
    ): ResponseEntity<*> = ResponseEntity.ok(
        threadService.createInBoard(requireNotNull(me.userId), boardId, req)
    )
}

// src/main/kotlin/com/rev/app/api/service/community/BoardController.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val threadService: ThreadService,
) {
    // (선택) 보드 헤더/정보
    @GetMapping("/{boardId}")
    fun getBoardHeader(@PathVariable boardId: Long): String =
        boardService.getBoardHeader(boardId)

    // 보드의 스레드 목록 (임시: 공개글만, 보드필터 추후 적용)
    @GetMapping("/{boardId}/threads")
    fun listThreads(
        @PathVariable boardId: Long,
        pageable: Pageable
    ): Page<ThreadRes> =
        boardService.listThreads(boardId, pageable)

    // 보드 내 특정 스레드 조회 (서비스는 엔티티 반환 → .toRes())
    @GetMapping("/{boardId}/threads/{threadId}")
    fun getThreadInBoard(
        @PathVariable boardId: Long,
        @PathVariable threadId: Long
    ): ThreadRes =
        threadService.get(threadId).toRes()
}

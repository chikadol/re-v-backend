package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.ThreadRes
import com.rev.app.api.service.community.toRes // ThreadEntity.toRes(), Board.toRes() 등 확장함수 임포트
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val threadService: ThreadService,
) {
    // ✅ Board 정보를 반환해야 하므로 BoardRes 리턴 & boardService.get(boardId) 사용
    @GetMapping("/{boardId}")
    fun get(@PathVariable boardId: Long): com.rev.app.api.service.community.dto.ThreadRes =
        boardService.get(boardId) // 여기서 이미 BoardRes를 리턴하도록 서비스가 설계되어 있어야 함

    // ✅ 보드 내 스레드 목록 (보드별 필터를 쓰지 않는 버전)
    @GetMapping("/{boardId}/threads")
    fun listThreads(
        @PathVariable boardId: Long,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<com.rev.app.api.service.community.dto.ThreadRes> =
        boardService.listThreads(pageable) // 서비스 시그니처가 pageable만 받는 버전일 때

    // (보드별 필터를 쓰고 싶다면 위 대신 아래 오버로드 사용)
    /*
    @GetMapping("/{boardId}/threads")
    fun listThreads(
        @PathVariable boardId: Long,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<ThreadRes> =
        boardService.listThreads(boardId, pageable)
    */

    // ✅ 단건 스레드 조회: threadService.get()이 ThreadEntity를 반환한다면 .toRes() 붙이기
    @GetMapping("/{boardId}/threads/{threadId}")
    fun getThreadInBoard(
        @PathVariable boardId: Long,
        @PathVariable threadId: Long
    ): com.rev.app.api.service.community.dto.ThreadRes =
        threadService.get(threadId).toRes()
}

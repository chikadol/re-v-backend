package com.rev.app.api.community

import com.rev.app.api.PageCursorResp
import com.rev.app.api.community.dto.*
import com.rev.app.service.community.BoardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Board")
@RestController
@RequestMapping("/boards")
class BoardController(private val svc: BoardService) {

    @Operation(summary = "보드 정보 조회")
    @GetMapping("/{slug}")
    fun getBoard(@PathVariable slug: String): BoardDto =
        boardDto(svc.getBoardBySlug(slug))

    @Operation(summary = "보드 스레드 목록 (커서 기반)")
    @GetMapping("/{slug}/threads")
    fun listThreads(
        @PathVariable slug: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int
    ): PageCursorResp<ThreadDto> {
        val page = svc.listThreadsBySlug(slug, size, cursor)
        return PageCursorResp(page.items.map(::threadDto), page.nextCursor)
    }
}

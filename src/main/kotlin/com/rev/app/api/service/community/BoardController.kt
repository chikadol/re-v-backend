// BoardController.kt
package com.rev.app.api.service.community

import com.rev.app.api.PageCursorResp
import com.rev.app.api.service.community.dto.ThreadDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService
) {
    @GetMapping("/{boardKey}/threads")
    fun listBoardThreads(
        @PathVariable boardKey: String,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageCursorResp<ThreadDto>> =
        ResponseEntity.ok(boardService.listThreads(boardKey, cursor, size))
}

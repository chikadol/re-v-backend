package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService
) {
    @GetMapping
    fun list(): ResponseEntity<List<BoardRes>> =
        ResponseEntity.ok(boardService.list())

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<BoardRes> =
        ResponseEntity.ok(boardService.get(id))
}

package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/boards", produces = [MediaType.APPLICATION_JSON_VALUE])
class BoardController(
    private val boardService: BoardService
) {
    @GetMapping
    fun list(): List<BoardRes> = boardService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): BoardRes = boardService.get(id)
}

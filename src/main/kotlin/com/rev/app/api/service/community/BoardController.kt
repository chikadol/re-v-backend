package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.BoardCreateRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @Valid @RequestBody request: BoardCreateRequest
    ): ResponseEntity<BoardRes> {
        val board = boardService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(board)
    }
}

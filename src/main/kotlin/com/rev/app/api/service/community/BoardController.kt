package com.rev.app.api.service.community

import com.rev.app.api.controller.ResponseHelper
import com.rev.app.api.controller.dto.ApiResponse
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
    fun list(): ResponseEntity<ApiResponse<List<BoardRes>>> {
        return try {
            val boards = boardService.list()
            ResponseHelper.ok(boards)
        } catch (e: Exception) {
            ResponseHelper.error("BOARD_LIST_FAILED", "게시판 목록을 불러오는 중 오류가 발생했습니다.")
        }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<ApiResponse<BoardRes>> {
        return try {
            val board = boardService.get(id)
            ResponseHelper.ok(board)
        } catch (e: Exception) {
            ResponseHelper.notFound("게시판을 찾을 수 없습니다.")
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @Valid @RequestBody request: BoardCreateRequest
    ): ResponseEntity<ApiResponse<BoardRes>> {
        return try {
            val board = boardService.create(request)
            ResponseHelper.ok(board, "게시판이 생성되었습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("BOARD_CREATE_FAILED", "게시판 생성 중 오류가 발생했습니다.")
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Nothing>> {
        return try {
            boardService.delete(id)
            ResponseHelper.ok<Nothing>(message = "게시판이 삭제되었습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("BOARD_DELETE_FAILED", "게시판 삭제 중 오류가 발생했습니다.")
        }
    }
}

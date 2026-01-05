package com.rev.app.api.service.community

import com.rev.app.api.error.ErrorCode
import com.rev.app.api.error.ResourceNotFoundException
import com.rev.app.api.error.ValidationException
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.BoardCreateRequest
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.BoardRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException
import java.util.UUID

@Service
class BoardService(
    private val boardRepository: BoardRepository
) {
    @Transactional(readOnly = true)
    fun list(): List<BoardRes> = boardRepository.findAll().map { it.toRes() }

    @Transactional(readOnly = true)
    fun get(id: UUID): BoardRes {
        return try {
            boardRepository.findById(id).orElseThrow().toRes()
        } catch (e: NoSuchElementException) {
            throw ResourceNotFoundException(
                errorCode = ErrorCode.BOARD_NOT_FOUND,
                message = "게시판을 찾을 수 없습니다: $id",
                details = mapOf("boardId" to id.toString())
            )
        }
    }

    @Transactional
    fun create(request: BoardCreateRequest): BoardRes {
        // slug 중복 확인
        val existing = boardRepository.findAll().find { it.slug == request.slug }
        if (existing != null) {
            throw ValidationException(
                errorCode = ErrorCode.BOARD_SLUG_DUPLICATE,
                message = "이미 사용 중인 슬러그입니다: ${request.slug}",
                details = mapOf("slug" to request.slug)
            )
        }

        val newBoard = Board(
            name = request.name,
            slug = request.slug,
            description = request.description
        )

        val savedBoard = boardRepository.saveAndFlush(newBoard)
        return savedBoard.toRes()
    }

    @Transactional
    fun delete(id: UUID) {
        if (!boardRepository.existsById(id)) {
            throw ResourceNotFoundException(
                errorCode = ErrorCode.BOARD_NOT_FOUND,
                message = "게시판을 찾을 수 없습니다: $id",
                details = mapOf("boardId" to id.toString())
            )
        }
        boardRepository.deleteById(id)
    }
}

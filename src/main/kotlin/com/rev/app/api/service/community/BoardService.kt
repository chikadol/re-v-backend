package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.ThreadRes
import com.rev.app.api.service.community.toRes
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val threadRepository: ThreadRepository
) {
    @Transactional(readOnly = true)
    fun get(boardId: Long): BoardRes =
        boardRepository.findById(boardId)
            .orElseThrow { NoSuchElementException("board $boardId not found") }
            .toRes()

    @Transactional(readOnly = true)
    fun listThreads(boardId: Long, pageable: Pageable): Page<ThreadRes> =
        threadRepository.findAllByBoard_IdAndIsPrivateFalse(boardId, pageable).map { it.toRes() }
}

package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.toEntity
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.auth.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> =
        threadRepository.findByBoard_IdAndIsPrivateFalse(boardId, pageable)
            .map { it.toRes() }

    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val board = boardRepository.getReferenceById(boardId)
        val author = userRepository.getReferenceById(userId)
        val entity = req.toEntity(board = board, author = author, parent = null)
        val saved = threadRepository.save(entity)
        return saved.toRes()
    }
}

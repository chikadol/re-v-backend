package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> =
        threadRepository.findByBoard_IdAndIsPrivateFalse(boardId, pageable).map { it.toRes() }

    @Transactional
    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val author = userRepository.getReferenceById(userId)
        val board = boardRepository.getReferenceById(boardId)
        val parent = req.parentThreadId?.let { threadRepository.getReferenceById(it) }

        val saved = threadRepository.save(
            ThreadEntity(
                title = req.title.trim(),
                content = req.content.trim(),
                author = author,
                board = board,
                isPrivate = req.isPrivate,
                categoryId = req.categoryId,
                parent = parent,
                tags = req.tags ?: emptyList()
            )
        )
        return saved.toRes()
    }
}

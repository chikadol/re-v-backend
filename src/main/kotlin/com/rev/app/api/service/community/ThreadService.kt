package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import com.rev.app.api.service.community.toRes
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.domain.community.Board


@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> =
        threadRepository.findByBoard_IdAndIsPrivateFalseOrderByCreatedAtDesc(boardId, pageable)
            .map { it.toRes() }

    @Transactional
    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val board: Board = boardRepository.getReferenceById(boardId)
        val author = userRepository.getReferenceById(userId)

        val entity = ThreadEntity(
            title = req.title.trim(),
            content = req.content,
            board = board,
            author = author,
            parent = req.parentId?.let { threadRepository.getReferenceById(it) },
            isPrivate = false,
            categoryId = null,
            tags = req.tags
        )
        return threadRepository.save(entity).toRes()
    }
}

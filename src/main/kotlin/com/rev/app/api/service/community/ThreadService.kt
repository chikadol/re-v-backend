package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.*
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.community.entity.ThreadEntity
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
    private val allowedSort = setOf("createdAt","updatedAt","title","id")

    private fun validateSort(pageable: Pageable) {
        pageable.sort.forEach {
            if (it.property !in allowedSort) {
                throw IllegalArgumentException("Unsupported sort key: ${it.property}")
            }
        }
    }

    @Transactional(readOnly = true)
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> {
        validateSort(pageable)
        return threadRepository
            .findByBoard_IdAndIsPrivateFalse(boardId, pageable)
            .map { it.toRes() }
    }

    @Transactional
    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val board = boardRepository.getReferenceById(boardId)
        val author = userRepository.getReferenceById(userId)

        val saved = threadRepository.save(
            ThreadEntity(
                title = req.title.trim(),
                content = req.content,
                board = board,
                parent = req.parentThreadId?.let { threadRepository.getReferenceById(it) },
                author = author,
                isPrivate = req.isPrivate,
                categoryId = req.categoryId,
                tags = req.tags
            )
        )
        return saved.toRes()
    }
}

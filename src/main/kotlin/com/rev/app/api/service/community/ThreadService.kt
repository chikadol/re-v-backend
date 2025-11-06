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
abstract class ThreadService(
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

    abstract fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes>
    abstract fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes
}

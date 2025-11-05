package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentRequest
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import com.rev.app.api.service.community.toRes
import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.ThreadRes

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun create(userId: UUID, req: CreateCommentRequest): CommentRes {
        val thread = threadRepository.getReferenceById(req.threadId)
        val author = userRepository.getReferenceById(userId)
        val parent = req.parentId?.let { commentRepository.getReferenceById(it) }

        val saved = commentRepository.save(
            CommentEntity(
                thread = thread,
                author = author,
                parent = parent,
                content = req.content
            )
        )
        return saved.toRes()
    }

    @Transactional(readOnly = true)
    fun listThreadComments(threadId: UUID): List<CommentRes> =
        commentRepository.findAllByThread_Id(threadId).map { it.toRes() }
}

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

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun create(userId: UUID, req: CreateCommentRequest): CommentRes {
        val thread = threadRepository.findById(req.threadId).orElseThrow()
        val parent = req.parentId?.let { commentRepository.findById(it).orElse(null) }
        val author = userRepository.findById(userId).orElseThrow()

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
        commentRepository.findAllByThreadId(threadId).map { it.toRes() }
}

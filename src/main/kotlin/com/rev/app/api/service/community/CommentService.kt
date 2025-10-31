
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateCommentReq
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

interface CommentService {
    fun addComment(threadId: Long, authorId: UUID, req: CreateCommentReq): CommentEntity
    fun listByThread(threadId: Long): List<CommentEntity>
}

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository
) : CommentService {

    override fun addComment(threadId: Long, authorId: UUID, req: CreateCommentReq): CommentEntity {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { NoSuchElementException("Thread $threadId not found") }
        val author = userRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("Author not found") }

        val entity = CommentEntity(
            thread = thread,
            author = author,
            content = req.content,
            parentId = req.parentId
        )
        return commentRepository.save(entity)
    }

    override fun listByThread(threadId: Long): List<CommentEntity> =
        commentRepository.findByThread_Id(threadId)
}

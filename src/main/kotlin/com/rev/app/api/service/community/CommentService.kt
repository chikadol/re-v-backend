package com.rev.app.api.service.community

import com.rev.app.api.controller.dto.CommentCreateRequest
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentRequest
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.notification.NotificationEntity
import com.rev.app.domain.notification.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import com.rev.app.api.service.community.dto.MyCommentRes
import com.rev.app.api.service.community.dto.toMyCommentRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository   // ✅ 추가
) {
    @Transactional
    fun create(authorId: UUID, req: CommentCreateRequest): CommentEntity {
        val author = userRepository.getReferenceById(authorId)
        val thread = threadRepository.getReferenceById(req.threadId)
        val parent = req.parentId?.let { commentRepository.getReferenceById(it) }

        val entity = CommentEntity(
            thread = thread,
            author = author,
            parent = parent,
            content = req.content,
        )

        return commentRepository.saveAndFlush(entity)
    }

    @Transactional(readOnly = true)
    fun listThreadComments(threadId: UUID): List<CommentRes> =
        commentRepository.findAllByThread_Id(threadId).map { it.toRes() }

    @Transactional(readOnly = true)
    fun listMine(authorId: UUID, pageable: Pageable): Page<MyCommentRes> =
        commentRepository
            .findAllByAuthor_Id(authorId, pageable)
            .map { it.toMyCommentRes() }
}

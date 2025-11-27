package com.rev.app.api.service.community

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

        // ✅ 알림 생성 대상 계산
        val recipients = mutableSetOf<UUID>()
        thread.author?.id?.let { if (it != userId) recipients.add(it) }       // 쓰레드 작성자
        parent?.author?.id?.let { if (it != userId) recipients.add(it) }      // 부모 댓글 작성자

        // ✅ 알림 저장
        if (recipients.isNotEmpty()) {
            val message = buildString {
                append("새 댓글: ")
                append(req.content.take(50))
            }
            recipients.forEach { uid ->
                val uref = userRepository.getReferenceById(uid)
                notificationRepository.save(
                    NotificationEntity(
                        receiver = uref,
                        type = "COMMENT",
                        thread = thread,
                        comment = saved,
                        message = message
                    )
                )
            }
        }

        return saved.toRes()
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

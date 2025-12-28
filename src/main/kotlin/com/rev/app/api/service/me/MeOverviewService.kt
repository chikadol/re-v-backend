package com.rev.app.api.service.me

import com.rev.app.api.service.me.dto.MeOverviewRes
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.notification.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.print.Pageable
import java.util.UUID

@Service
class MeOverviewService(
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val threadBookmarkRepository: ThreadBookmarkRepository,
    private val notificationRepository: NotificationRepository
) {

    @Transactional(readOnly = true)
    fun getOverview(userId: UUID): MeOverviewRes {
        val threadCount = threadRepository.countByAuthor_Id(userId)
        val commentCount = commentRepository.countByAuthor_Id(userId)
        val bookmarkCount: Long = threadBookmarkRepository.countByUser_Id(userId)
        val unreadNotificationCount = notificationRepository.countByReceiver_IdAndIsReadFalse(userId)

        return MeOverviewRes(
            threadCount = threadCount,
            commentCount = commentCount,
            bookmarkCount = bookmarkCount,
            unreadNotificationCount = unreadNotificationCount
        )
    }
}

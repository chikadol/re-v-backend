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

    fun getOverview(userId: UUID): MeOverviewRes {
        val threadCount = getThreadCount(userId)
        val commentCount = getCommentCount(userId)
        val bookmarkCount = getBookmarkCount(userId)
        val unreadNotificationCount = getUnreadNotificationCount(userId)

        return MeOverviewRes(
            threadCount = threadCount,
            commentCount = commentCount,
            bookmarkCount = bookmarkCount,
            unreadNotificationCount = unreadNotificationCount
        )
    }
    
    @Transactional(readOnly = true)
    private fun getThreadCount(userId: UUID): Long {
        return try {
            threadRepository.countByAuthor_Id(userId)
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getCommentCount(userId: UUID): Long {
        return try {
            commentRepository.countByAuthor_Id(userId)
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getBookmarkCount(userId: UUID): Long {
        return try {
            threadBookmarkRepository.countByUser_Id(userId)
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getUnreadNotificationCount(userId: UUID): Long {
        return try {
            notificationRepository.countByReceiver_IdAndIsReadFalse(userId)
        } catch (e: Exception) {
            0L
        }
    }
}

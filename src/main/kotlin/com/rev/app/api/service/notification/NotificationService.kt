package com.rev.app.api.service.notification

import com.rev.app.api.service.notification.dto.NotificationRes
import com.rev.app.api.service.notification.dto.toRes
import com.rev.app.domain.notification.NotificationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    @Transactional(readOnly = true)
    fun listMine(userId: UUID, pageable: Pageable, type: String? = null): Page<NotificationRes> {
        // type 필터는 나중에 구현 가능
        return notificationRepository.findAllByReceiver_IdOrderByCreatedAtDesc(userId, pageable).map { it.toRes() }
    }

    @Transactional
    fun markRead(userId: UUID, notificationId: UUID): NotificationRes {
        val n = notificationRepository.findById(notificationId).orElseThrow()
        require(n.receiver.id == userId) { "Forbidden" }
        n.isRead = true
        return notificationRepository.save(n).toRes()
    }

    @Transactional
    fun markAllRead(userId: UUID) {
        val page = notificationRepository
            .findAllByReceiver_IdOrderByCreatedAtDesc(userId, Pageable.ofSize(500))
        val items = page.content.onEach { it.isRead = true }   // ✅ content만 저장
        notificationRepository.saveAll(items)
    }

    @Transactional(readOnly = true)
    fun unreadCount(userId: UUID): Long =
        notificationRepository.countByReceiver_IdAndIsReadFalse(userId)
}

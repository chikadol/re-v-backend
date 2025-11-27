package com.rev.app.domain.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun countByReceiver_IdAndIsReadFalse(receiverId: UUID): Long
    // 읽지 않은 알림 목록
    fun findAllByReceiver_IdAndIsReadFalse(userId: UUID): List<NotificationEntity>
    // 읽지 않은 알림 개수
    fun findAllByReceiver_IdOrderByCreatedAtDesc(
        receiverId: UUID,
        pageable: Pageable
    ): Page<NotificationEntity>

}

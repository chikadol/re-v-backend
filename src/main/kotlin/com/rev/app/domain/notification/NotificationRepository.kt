package com.rev.app.domain.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun findAllByUser_IdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<NotificationEntity>
    fun countByReceiver_IdAndIsReadFalse(receiverId: UUID): Long
    // 읽지 않은 알림 목록
    fun findAllByUser_IdAndIsReadFalse(userId: UUID): List<NotificationEntity>

    // 읽지 않은 알림 개수
    fun countByUser_IdAndIsReadFalse(userId: UUID): Long
    fun findAllByReceiver_IdOrderByCreatedAtDesc(
        receiverId: UUID,
        pageable: Pageable
    ): Page<NotificationEntity>

}

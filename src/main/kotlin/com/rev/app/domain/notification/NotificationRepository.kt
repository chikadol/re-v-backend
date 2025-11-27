package com.rev.app.domain.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun findAllByUser_IdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<NotificationEntity>
    fun countByUser_IdAndIsReadFalse(userId: UUID): Long
    fun countByReceiver_IdAndIsReadFalse(receiverId: UUID): Long
}

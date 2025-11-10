package com.rev.app.api.service.notification.dto

import java.time.Instant
import java.util.UUID

data class NotificationRes(
    val id: UUID,
    val type: String,
    val threadId: UUID,
    val commentId: UUID,
    val message: String,
    val isRead: Boolean,
    val createdAt: Instant
)

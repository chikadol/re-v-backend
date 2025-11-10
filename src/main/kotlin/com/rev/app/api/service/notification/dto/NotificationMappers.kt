package com.rev.app.api.service.notification.dto

import com.rev.app.domain.notification.NotificationEntity

fun NotificationEntity.toRes() = NotificationRes(
    id = requireNotNull(id),
    type = type,
    threadId = requireNotNull(thread.id),
    commentId = requireNotNull(comment.id),
    message = message,
    isRead = isRead,
    createdAt = createdAt
)

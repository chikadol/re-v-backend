package com.rev.app.api.service.community.entity

import java.time.Instant
import java.util.UUID

data class ThreadBookmarkDto(
    val id: Long,
    val threadId: Long,
    val userId: UUID,
    val createdAt: Instant
)

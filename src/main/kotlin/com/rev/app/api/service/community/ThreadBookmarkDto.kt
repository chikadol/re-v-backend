// src/main/kotlin/com/rev/app/api/service/community/dto/ThreadBookmarkDto.kt
package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class ThreadBookmarkDto(
    val id: Long,
    val threadId: Long,
    val userId: UUID,
    val createdAt: Instant
)

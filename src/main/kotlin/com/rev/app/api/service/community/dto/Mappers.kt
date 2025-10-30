package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.*
import java.time.Instant
import java.util.UUID

data class CommentDto(
    val id: Long,
    val threadId: Long,
    val authorId: UUID,
    val content: String,
    val parentId: Long?,
    val createdAt: Instant,      // non-null
    val updatedAt: Instant?
)
package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.*
import java.time.Instant
import java.util.UUID

data class CommentDto(
    val id: Long,
    val threadId: Long,
    val authorId: UUID,
    val content: String,
    val parentId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)
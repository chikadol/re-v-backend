package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class CommentRes(
    val id: UUID,
    val threadId: UUID?,
    val authorId: UUID?,
    val content: String,
    val parentId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant
)

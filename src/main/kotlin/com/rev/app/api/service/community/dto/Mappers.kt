package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.*
import java.time.Instant

data class CommentDto(
    val id: Long,
    val threadId: Long,
    val authorId: Long,
    val content: String,
    val parentId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)
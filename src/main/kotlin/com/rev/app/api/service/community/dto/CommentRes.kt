// src/main/kotlin/com/rev/app/api/service/community/dto/CommentRes.kt
package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class CommentRes(
    val id: Long,
    val threadId: Long,
    val authorId: UUID,
    val content: String,
    val parentId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant?
)
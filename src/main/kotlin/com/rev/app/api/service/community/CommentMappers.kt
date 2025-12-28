package com.rev.app.api.service.community

import com.rev.app.domain.community.entity.CommentEntity
import java.time.Instant
import java.util.UUID

data class CommentDto(
    val id: UUID,
    val threadId: UUID?,
    val authorId: UUID?,
    val parentId: UUID?,
    val content: String,
    val createdAt: Instant?
)

fun CommentEntity.toDto(): CommentDto =
    CommentDto(
        id = requireNotNull(id),
        threadId = thread?.id,
        authorId = author?.id,
        parentId = parent?.id,
        content = content,
        createdAt = createdAt
    )

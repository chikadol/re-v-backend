package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.*
import java.time.Instant
import java.util.UUID

import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.domain.community.entity.ThreadEntity

fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
        id = requireNotNull(this.id),
        title = this.title,
        content = this.content,
        boardId = this.board?.id,              // ✅ 채워줌
        parentThreadId = this.parent?.id,
        authorId = this.author?.id,
        isPrivate = this.isPrivate,
        categoryId = this.categoryId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        tags = this.tags ?: emptyList()
    )
data class CommentDto(
    val id: Long,
    val threadId: Long,
    val authorId: UUID,
    val content: String,
    val parentId: Long?,
    val createdAt: Instant,      // non-null
    val updatedAt: Instant?
)
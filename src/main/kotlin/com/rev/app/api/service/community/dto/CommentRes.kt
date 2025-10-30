package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class CommentRes(
    val id: Long?,
    val threadId: Long,
    val authorId: UUID,
    val content: String,
    val parentId: Long?,
    val createdAt: Instant?,
    val updatedAt: Instant?
) {
    companion object {
        fun from(dto: CommentDto) = CommentRes(
            id = dto.id,
            threadId = dto.threadId,
            authorId = dto.authorId,
            content = dto.content,
            parentId = dto.parentId,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
}

package com.rev.app.api.controller.dto

import com.rev.app.domain.community.entity.CommentEntity
import java.time.Instant
import java.util.UUID

data class CommentResponse(
    val id: UUID,
    val threadId: UUID,
    val authorId: UUID,
    val parentId: UUID?,
    val content: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(entity: CommentEntity): CommentResponse? =
            entity.createdAt?.let {
                CommentResponse(
                    id = entity.id!!,
                    threadId = entity.thread.id!!,
                    authorId = entity.author.id!!,
                    parentId = entity.parent?.id,
                    content = entity.content,
                    createdAt = it,
                )
            }
    }
}

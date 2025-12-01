package com.rev.app.api.controller.dto

import com.rev.app.domain.community.entity.ThreadEntity
import java.time.Instant
import java.util.*

data class ThreadResponse(
    val id: UUID,
    val boardId: UUID,
    val authorId: UUID?,
    val title: String,
    val content: String,
    val createdAt: Instant
) {
    companion object {
        fun from(entity: ThreadEntity): ThreadResponse? =
            entity.id?.let {
                entity.board?.id?.let { boardId ->
                    entity.createdAt?.let { createdAt ->
                        ThreadResponse(
                            id = it,
                            boardId = boardId,
                            authorId = entity.author?.id,
                            title = entity.title,
                            content = entity.content,
                            createdAt = createdAt
                        )
                    }
                }
            }
    }
}

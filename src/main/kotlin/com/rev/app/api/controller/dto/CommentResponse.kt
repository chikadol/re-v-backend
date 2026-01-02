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
    val isAuthor: Boolean = false // 게시물 작성자인지 여부
) {
    companion object {
        fun from(entity: CommentEntity): CommentResponse? =
            entity.createdAt?.let {
                // 게시물 작성자와 댓글 작성자가 같은지 확인
                val threadAuthorId = entity.thread?.author?.id
                val commentAuthorId = entity.author?.id
                val isAuthor = threadAuthorId != null && 
                               commentAuthorId != null && 
                               threadAuthorId == commentAuthorId
                
                CommentResponse(
                    id = entity.id!!,
                    threadId = entity.thread.id!!,
                    authorId = entity.author.id!!,
                    parentId = entity.parent?.id,
                    content = entity.content,
                    createdAt = it,
                    isAuthor = isAuthor
                )
            }
    }
}

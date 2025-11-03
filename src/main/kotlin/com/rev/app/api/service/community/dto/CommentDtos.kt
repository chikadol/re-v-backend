package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class CreateCommentReq(
    val content: String,
    val parentId: Long? = null
)

data class UpdateCommentReq(
    val content: String
)

data class CommentRes(
    val id: Long,
    val threadId: Long,
    val authorId: UUID,
    val content: String,
    val parentId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

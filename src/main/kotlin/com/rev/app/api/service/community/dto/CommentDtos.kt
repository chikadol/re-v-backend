package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class MyCommentRes(
    val commentId: UUID,
    val threadId: UUID,
    val threadTitle: String,
    val boardId: UUID?,
    val boardName: String?,
    val content: String,
    val createdAt: Instant?
)

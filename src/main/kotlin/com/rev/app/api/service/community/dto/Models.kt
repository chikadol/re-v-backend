package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class BoardRes(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?
)

data class ThreadRes(
    val id: UUID,
    val title: String,
    val content: String,
    val boardId: UUID?,
    val parentThreadId: UUID?,
    val authorId: UUID?,
    val isPrivate: Boolean,
    val categoryId: UUID?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val tags: List<String> = emptyList()
)

data class CreateThreadReq(
    val title: String,
    val content: String,
    val parentThreadId: UUID? = null,
    val tags: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val categoryId: UUID? = null
)

data class CommentRes(
    val id: UUID,
    val threadId: UUID,
    val authorId: UUID,
    val parentId: UUID?,
    val content: String,
    val createdAt: Instant?
)

data class CreateCommentRequest(
    val threadId: UUID,
    val parentId: UUID? = null,
    val content: String
)

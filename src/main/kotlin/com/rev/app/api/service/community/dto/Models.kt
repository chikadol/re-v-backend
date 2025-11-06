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

data class CommentRes(
    val id: UUID,
    val threadId: UUID,
    val parentId: UUID?,
    val authorId: UUID?,
    val content: String,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

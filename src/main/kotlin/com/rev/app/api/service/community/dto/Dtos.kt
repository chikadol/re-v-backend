package com.rev.app.api.service.community.dto

import java.time.Instant

data class ThreadCreateReq(
    val boardSlug: String,
    val title: String,
    val content: String? = null,
    val isAnonymous: Boolean = false
)

data class BoardDto(
    val id: Long, val slug: String, val name: String,
    val isAnonymousAllowed: Boolean, val createdAt: Instant
)

data class ThreadDto(
    val id: Long, val boardId: Long, val authorId: Long,
    val title: String, val displayNo: Long, val createdAt: Instant
)

data class ThreadDetailDto(
    val id: Long, val boardId: Long, val authorId: Long,
    val title: String, val content: String?,
    val isAnonymous: Boolean, val displayNo: Long,
    val viewCount: Long, val likeCount: Int, val dislikeCount: Int, val commentCount: Int,
    val pinnedUntil: Instant?, val deletedAt: Instant?, val createdAt: Instant, val updatedAt: Instant
)

data class CommentDto(
    val id: Long, val threadId: Long, val parentId: Long?,
    val authorId: Long, val content: String, val isAnonymous: Boolean,
    val likeCount: Int, val deletedAt: Instant?, val createdAt: Instant
)

data class ToggleResultDto(val ok: Boolean, val state: Boolean? = null)

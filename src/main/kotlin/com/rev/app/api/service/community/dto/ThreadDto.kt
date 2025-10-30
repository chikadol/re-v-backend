package com.rev.app.api.service.community.dto

import com.rev.app.api.service.community.ReactionType
import java.time.Instant
import java.util.UUID

data class ThreadDto(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: UUID,
    val tags: List<String> = emptyList(),
    val categoryId: Long? = null,
    val parentThreadId: Long? = null,
    val isPrivate: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val myReaction: ReactionType? = null,
    val reactionCounts: Map<ReactionType, Long> = emptyMap(),
    val bookmarked: Boolean = false
)
package com.rev.app.api.service.community

import com.rev.app.api.service.community.ReactionType
import java.time.Instant
import java.util.UUID

data class CreateThreadReq(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val categoryId: Long? = null,
    val parentThreadId: Long? = null,
    val isPrivate: Boolean = false
)
/*

data class ThreadDto(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val tags: List<String>,
    val categoryId: UUID?,
    val parentThreadId: UUID?,
    val isPrivate: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val myReaction: ReactionType?,
    val reactionCounts: Map<ReactionType, Long>,
    val bookmarked: Boolean
)
*/

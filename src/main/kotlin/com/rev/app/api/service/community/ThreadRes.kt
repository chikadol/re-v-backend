package com.rev.app.api.service.community

import java.time.Instant
import java.util.Collections.emptyList
import java.util.UUID

data class ThreadRes(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: UUID,
    val tags: List<String> = emptyList(),
    val categoryId: UUID? = null,
    val parentThreadId: Long? = null,
    val isPrivate: Boolean = false,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)

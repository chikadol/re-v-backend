package com.rev.app.api.service.community

import java.time.Instant
import java.util.UUID

data class ThreadRes(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: UUID,              // ✅ Long → UUID
    val tags: List<String>,
    val categoryId: UUID?,           // (엔티티가 UUID면 유지)
    val parentThreadId: Long?,       // (스레드 PK가 Long이면 Long? 유지)
    val isPrivate: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

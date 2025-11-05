package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

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
    val tags: List<String> = emptyList() // 엔티티에 없으면 빈 리스트로 내려줌
)

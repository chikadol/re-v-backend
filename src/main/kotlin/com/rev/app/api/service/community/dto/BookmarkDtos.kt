package com.rev.app.api.service.community.dto

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class MyBookmarkedThreadRes(
    val threadId: UUID,
    val title: String,
    val boardId: UUID?,
    val boardName: String?,
    val createdAt: Instant?,      // ThreadEntity.createdAt 타입에 맞추기
    val bookmarkedAt: Instant?    // 일단 nullable
)
package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.BoardRequestEntity
import com.rev.app.domain.community.BoardRequestStatus
import java.time.Instant
import java.util.UUID

data class BoardRequestRes(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?,
    val reason: String?,
    val requesterId: UUID,
    val requesterUsername: String?,
    val status: BoardRequestStatus,
    val createdAt: Instant?,
    val processedAt: Instant?,
    val processedById: UUID?,
    val processedByUsername: String?
)

data class BoardRequestCreateRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val reason: String? = null
)

data class BoardRequestProcessRequest(
    val approved: Boolean,
    val comment: String? = null // 승인/거부 사유
)

fun BoardRequestEntity.toRes(): BoardRequestRes = BoardRequestRes(
    id = requireNotNull(id),
    name = name,
    slug = slug,
    description = description,
    reason = reason,
    requesterId = requireNotNull(requester.id),
    requesterUsername = requester.username,
    status = status,
    createdAt = createdAt,
    processedAt = processedAt,
    processedById = processedBy?.id,
    processedByUsername = processedBy?.username
)


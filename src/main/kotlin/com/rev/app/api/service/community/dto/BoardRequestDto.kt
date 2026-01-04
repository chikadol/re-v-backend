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
    @field:jakarta.validation.constraints.NotBlank(message = "게시판 이름은 필수 항목입니다.")
    @field:jakarta.validation.constraints.Size(min = 1, max = 100, message = "게시판 이름은 1자 이상 100자 이하여야 합니다.")
    val name: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "슬러그는 필수 항목입니다.")
    @field:jakarta.validation.constraints.Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "슬러그는 소문자, 숫자, 하이픈(-)만 사용할 수 있습니다."
    )
    val slug: String,
    
    @field:jakarta.validation.constraints.Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    val description: String? = null,
    
    @field:jakarta.validation.constraints.Size(max = 1000, message = "신청 사유는 1000자 이하여야 합니다.")
    val reason: String? = null
)

data class BoardRequestProcessRequest(
    @field:jakarta.validation.constraints.NotNull(message = "승인 여부는 필수 항목입니다.")
    val approved: Boolean,
    
    @field:jakarta.validation.constraints.Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
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


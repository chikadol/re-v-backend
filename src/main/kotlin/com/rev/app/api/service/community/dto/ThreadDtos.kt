package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class UpdateThreadReq(
    @field:Size(max = 200) val title: String?,
    val content: String?,
    val tags: List<String>?,
    val categoryId: UUID?,
    val isPrivate: Boolean?
)

data class ThreadRes(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: UUID,
    val tags: List<String>,
    val categoryId: UUID?,
    val parentThreadId: Long?,
    val isPrivate: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

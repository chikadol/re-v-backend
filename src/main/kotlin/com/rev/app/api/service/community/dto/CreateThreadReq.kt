package com.rev.app.api.service.community.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateThreadReq(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    val tags: List<String> = emptyList(),
    val categoryId: UUID? = null,
    val parentThreadId: Long? = null,
    val isPrivate: Boolean = false
)
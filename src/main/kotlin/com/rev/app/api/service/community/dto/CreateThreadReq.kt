package com.rev.app.api.service.community.dto

import java.util.UUID

data class CreateThreadReq(
    @field:jakarta.validation.constraints.NotBlank
    val title: String,
    @field:jakarta.validation.constraints.NotBlank
    val content: String,
    val tags: List<String> = emptyList(),
    val categoryId: java.util.UUID? = null,
    val parentThreadId: Long? = null,
    val isPrivate: Boolean = false
)

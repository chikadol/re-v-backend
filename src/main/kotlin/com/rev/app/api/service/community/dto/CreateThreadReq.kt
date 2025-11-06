package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class CreateThreadReq(
    val title: String,
    val content: String,
    val parentThreadId: UUID? = null,
    val isPrivate: Boolean = false,
    val categoryId: UUID? = null,
    val tags: List<String>? = emptyList()
)

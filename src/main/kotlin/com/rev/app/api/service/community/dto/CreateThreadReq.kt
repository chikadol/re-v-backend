package com.rev.app.api.service.community.dto

import java.util.UUID

data class CreateThreadReq(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val categoryId: UUID? = null,
    val parentThreadId: Long? = null,
    val isPrivate: Boolean = false
)

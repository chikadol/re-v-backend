package com.rev.app.api.service.community

import java.util.UUID


data class CreateThreadReq(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val categoryId: UUID? = null,
    val parentId: UUID? = null,
    val isPrivate: Boolean = false
)

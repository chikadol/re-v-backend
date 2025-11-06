package com.rev.app.api.service.community.dto

import java.util.UUID

data class CreateCommentRequest(
    val threadId: UUID,
    val parentId: UUID? = null,
    val content: String
)

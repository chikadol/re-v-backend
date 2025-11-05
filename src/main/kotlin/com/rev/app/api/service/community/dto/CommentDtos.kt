package com.rev.app.api.service.community.dto

import java.time.Instant
import java.util.UUID

data class CreateCommentReq(
    val content: String,
    val parentId: Long? = null
)

data class UpdateCommentReq(
    val content: String
)


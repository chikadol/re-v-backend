package com.rev.app.api.service.community.dto

import jakarta.validation.constraints.NotBlank
import java.util.*

data class CreateCommentRequest(
    val threadId: UUID,
    val parentId: UUID? = null,
    @field:NotBlank
    val content: String
)

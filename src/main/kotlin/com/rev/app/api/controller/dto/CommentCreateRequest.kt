package com.rev.app.api.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CommentCreateRequest(

    @JsonProperty("threadId")
    val threadId: UUID,

    @JsonProperty("parentId")
    val parentId: UUID? = null,

    @field:NotBlank
    @field:Size(min = 1)
    val content: String
)

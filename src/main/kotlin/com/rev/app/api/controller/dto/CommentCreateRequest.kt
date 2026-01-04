package com.rev.app.api.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class CommentCreateRequest(

    @JsonProperty("threadId")
    @field:NotNull(message = "게시글 ID는 필수 항목입니다.")
    val threadId: UUID,

    @JsonProperty("parentId")
    val parentId: UUID? = null,

    @field:NotBlank(message = "댓글 내용은 필수 항목입니다.")
    @field:Size(min = 1, max = 5000, message = "댓글 내용은 1자 이상 5000자 이하여야 합니다.")
    val content: String
)

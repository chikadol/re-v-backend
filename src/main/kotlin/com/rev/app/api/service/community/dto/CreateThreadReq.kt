package com.rev.app.api.service.community.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

@Schema(name="CreateThreadReq", description="스레드 생성")
data class CreateThreadReq(
    @field:NotBlank
    @Schema(example="Hello")
    val title: String,
    @field:NotBlank
    @Schema(example="World")
    val content: String,
    @Schema(example="[\"tag1\",\"tag2\"]")
    val tags: List<String> = emptyList(),
    val parentThreadId: UUID? = null
)


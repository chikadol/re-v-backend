package com.rev.app.api.controller.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "게시글 생성 요청")
data class ThreadCreateRequest(
    @field:NotBlank(message = "제목은 필수 항목입니다.")
    @field:Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
    @Schema(description = "게시글 제목", example = "새로운 게시글", required = true, minLength = 1, maxLength = 200)
    val title: String,
    
    @field:NotBlank(message = "내용은 필수 항목입니다.")
    @field:Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하여야 합니다.")
    @Schema(description = "게시글 내용", example = "게시글 내용입니다.", required = true, minLength = 1, maxLength = 10000)
    val content: String,
    
    @Schema(description = "비공개 여부", example = "false", defaultValue = "false")
    val isPrivate: Boolean = false
)

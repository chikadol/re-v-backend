package com.rev.app.api.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ThreadCreateRequest(
    @field:NotBlank(message = "제목은 필수 항목입니다.")
    @field:Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
    val title: String,
    
    @field:NotBlank(message = "내용은 필수 항목입니다.")
    @field:Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하여야 합니다.")
    val content: String,
    
    val isPrivate: Boolean = false
)

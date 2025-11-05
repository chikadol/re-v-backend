package com.rev.app.api.service.community.dto

import jakarta.validation.constraints.NotBlank

data class CreateThreadReq(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    // 엔티티에 tags 필드가 없다면 DTO에는 남겨도 매핑에서 무시하거나, 아예 뺄 수 있음.
    val tags: List<String> = emptyList()
)

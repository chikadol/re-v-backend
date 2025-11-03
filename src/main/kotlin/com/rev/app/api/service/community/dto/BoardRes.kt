package com.rev.app.api.service.community.dto

data class BoardRes(
    val id: Long,
    val slug: String,
    val name: String,
    val description: String?    // 유지
)

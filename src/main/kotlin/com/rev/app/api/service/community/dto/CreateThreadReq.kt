package com.rev.app.api.service.community.dto

data class CreateThreadReq(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)

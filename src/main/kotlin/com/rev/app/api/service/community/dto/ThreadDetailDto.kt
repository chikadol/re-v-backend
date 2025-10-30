package com.rev.app.api.service.community.dto

data class ThreadDetailDto(
    val thread: ThreadDto,
    val comments: List<CommentDto> = emptyList()
)
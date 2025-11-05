package com.rev.app.api.service.community.dto

import com.rev.app.api.service.community.CommentDto

data class ThreadDetailDto(
    val thread: ThreadDto,
    val comments: List<CommentDto> = emptyList()
)
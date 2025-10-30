package com.rev.app.api.service.community.dto

data class CreateCommentReq(
    val content: String,
    val parentId: Long? = null
)

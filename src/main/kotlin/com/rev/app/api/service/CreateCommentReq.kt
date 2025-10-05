package com.rev.app.api.service

data class CreateCommentReq(
    val threadId: Long,
    val content: String,
    val parentId: Long? = null,
    val isAnonymous: Boolean = false
)

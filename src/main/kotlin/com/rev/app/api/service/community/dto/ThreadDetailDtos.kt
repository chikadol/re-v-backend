package com.rev.app.api.service.community.dto

import java.util.UUID

data class ThreadDetailRes(
    val thread: ThreadRes,
    val commentCount: Long,
    val bookmarkCount: Long,
    val reactions: Map<String, Long>,
    val myReaction: String?,
    val bookmarked: Boolean
)
package com.rev.app.api

data class PageCursorResp<T>(
    val items: List<T>,
    val nextCursor: Long?
)
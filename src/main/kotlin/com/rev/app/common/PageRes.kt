package com.rev.app.common

import org.springframework.data.domain.Page

data class PageRes<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

fun <T> Page<T>.toRes() = PageRes(
    content = content,
    page = number,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)

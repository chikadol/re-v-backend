package com.rev.app.api

import org.springframework.data.domain.Page

data class PageRes<T>(val content: List<T>, val page:Int, val size:Int, val totalElements:Long, val totalPages:Int)
fun <T> Page<T>.toRes() = PageRes(content, number, size, totalElements, totalPages)
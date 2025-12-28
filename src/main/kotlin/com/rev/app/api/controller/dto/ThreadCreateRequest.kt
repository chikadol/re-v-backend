package com.rev.app.api.controller.dto

data class ThreadCreateRequest(
    val title: String,
    val content: String,
    val isPrivate: Boolean = false
)

package com.rev.app.api.error

data class ApiErrorResponse(
    val code: String,
    val message: String,
    val details: Any? = null
)

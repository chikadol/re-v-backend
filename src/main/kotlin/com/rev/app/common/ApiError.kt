package com.rev.app.common

import java.time.Instant


data class ApiError(
    val timestamp: Instant = Instant.now(),
    val code: String,
    val message: String,
    val path: String? = null
)

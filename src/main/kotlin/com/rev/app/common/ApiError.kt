package com.rev.app.common

import org.springframework.validation.BindingResult

data class ApiError(
    val message: String,
    val details: Map<String, String>? = null
) {
    companion object {
        fun fromBindingResult(
            br: BindingResult,
            message: String = "Validation failed"
        ): ApiError {
            val details = br.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
            return ApiError(message, if (details.isEmpty()) null else details)
        }
    }
}

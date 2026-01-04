package com.rev.app.api.controller.dto

/**
 * 통일된 API 응답 포맷
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ApiError? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        fun <T> success(message: String): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = null
            )
        }

        fun <T> error(error: ApiError, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = error,
                message = message ?: error.message
            )
        }

        fun <T> error(code: String, message: String, details: Map<String, Any>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ApiError(code, message, details)
            )
        }
    }
}

/**
 * API 에러 정보
 */
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)


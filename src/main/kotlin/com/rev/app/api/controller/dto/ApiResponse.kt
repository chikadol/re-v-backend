package com.rev.app.api.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 통일된 API 응답 포맷
 */
@Schema(description = "통일된 API 응답 형식")
data class ApiResponse<T>(
    @Schema(description = "요청 성공 여부", example = "true")
    val success: Boolean,
    
    @Schema(description = "응답 데이터")
    val data: T? = null,
    
    @Schema(description = "성공/실패 메시지", example = "요청이 성공적으로 처리되었습니다.")
    val message: String? = null,
    
    @Schema(description = "에러 정보 (실패 시)")
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
@Schema(description = "API 에러 정보")
data class ApiError(
    @Schema(description = "에러 코드", example = "THREAD_NOT_FOUND")
    val code: String,
    
    @Schema(description = "에러 메시지", example = "게시글을 찾을 수 없습니다.")
    val message: String,
    
    @Schema(description = "추가 에러 상세 정보")
    val details: Map<String, Any>? = null
)


package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * 컨트롤러에서 통일된 응답을 생성하기 위한 헬퍼 함수
 */
object ResponseHelper {
    /**
     * 성공 응답 생성
     */
    fun <T> ok(data: T, message: String? = null): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.ok(ApiResponse.success(data, message))
    }

    /**
     * 메시지만 있는 성공 응답 생성
     */
    fun <T> ok(message: String): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.ok(ApiResponse.success<T>(message))
    }

    /**
     * 페이징된 데이터 성공 응답 생성 (Page<T>에서 PageResponse로 변환)
     */
    fun <T> ok(page: Page<T>, message: String? = null): ResponseEntity<ApiResponse<PageResponse<T>>> {
        val pageResponse = PageResponse(
            content = page.content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            number = page.number,
            size = page.size,
            first = page.isFirst,
            last = page.isLast
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse, message))
    }

    /**
     * PageResponse를 직접 받는 성공 응답 생성 (캐시된 PageResponse 사용)
     */
    fun <T> ok(pageResponse: PageResponse<T>, message: String? = null): ResponseEntity<ApiResponse<PageResponse<T>>> {
        return ResponseEntity.ok(ApiResponse.success(pageResponse, message))
    }

    /**
     * 에러 응답 생성
     */
    fun <T> error(
        code: String,
        message: String,
        status: HttpStatus = HttpStatus.BAD_REQUEST,
        details: Map<String, Any>? = null
    ): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.status(status)
            .body(ApiResponse.error<T>(code, message, details))
    }

    /**
     * 인증 에러 응답 생성
     */
    fun <T> unauthorized(message: String = "인증이 필요합니다."): ResponseEntity<ApiResponse<T>> {
        return error("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED)
    }

    /**
     * 권한 에러 응답 생성
     */
    fun <T> forbidden(message: String = "권한이 없습니다."): ResponseEntity<ApiResponse<T>> {
        return error("FORBIDDEN", message, HttpStatus.FORBIDDEN)
    }

    /**
     * 리소스 없음 에러 응답 생성
     */
    fun <T> notFound(message: String = "리소스를 찾을 수 없습니다."): ResponseEntity<ApiResponse<T>> {
        return error("NOT_FOUND", message, HttpStatus.NOT_FOUND)
    }

    /**
     * 서버 에러 응답 생성
     */
    fun <T> internalError(message: String = "서버 오류가 발생했습니다."): ResponseEntity<ApiResponse<T>> {
        return error("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

/**
 * 페이징 응답 데이터
 */
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean
)


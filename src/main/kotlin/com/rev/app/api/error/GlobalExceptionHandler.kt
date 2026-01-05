package com.rev.app.api.error

import com.rev.app.api.controller.ResponseHelper
import com.rev.app.api.controller.dto.ApiResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.NoSuchElementException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("BusinessException: [{}] {}", ex.errorCode.code, ex.message, ex)
        
        return ResponseHelper.error(
            code = ex.errorCode.code,
            message = ex.message ?: ex.errorCode.message,
            status = ex.errorCode.httpStatus,
            details = ex.details
        )
    }
    
    /**
     * 리소스 없음 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("ResourceNotFoundException: [{}] {}", ex.errorCode.code, ex.message, ex)
        
        return ResponseHelper.error(
            code = ex.errorCode.code,
            message = ex.message ?: ex.errorCode.message,
            status = ex.errorCode.httpStatus,
            details = ex.details
        )
    }
    
    /**
     * 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("AuthenticationException: [{}] {}", ex.errorCode.code, ex.message, ex)
        
        return ResponseHelper.error(
            code = ex.errorCode.code,
            message = ex.message ?: ex.errorCode.message,
            status = ex.errorCode.httpStatus,
            details = ex.details
        )
    }
    
    /**
     * 권한 예외 처리
     */
    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(ex: AuthorizationException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("AuthorizationException: [{}] {}", ex.errorCode.code, ex.message, ex)
        
        return ResponseHelper.error(
            code = ex.errorCode.code,
            message = ex.message ?: ex.errorCode.message,
            status = ex.errorCode.httpStatus,
            details = ex.details
        )
    }
    
    /**
     * Spring Security AccessDeniedException 처리
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("AccessDeniedException: {}", ex.message, ex)
        
        return ResponseHelper.error(
            code = ErrorCode.FORBIDDEN.code,
            message = ErrorCode.FORBIDDEN.message,
            status = ErrorCode.FORBIDDEN.httpStatus
        )
    }
    
    /**
     * NoSuchElementException 처리 (Optional.get() 등에서 발생)
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("NoSuchElementException: {}", ex.message, ex)
        
        return ResponseHelper.error(
            code = ErrorCode.NOT_FOUND.code,
            message = ex.message ?: ErrorCode.NOT_FOUND.message,
            status = ErrorCode.NOT_FOUND.httpStatus
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Nothing>> {

        val fieldErrors = ex.bindingResult.fieldErrors.associate { fieldError ->
            val field = fieldError.field
            val message = getKoreanMessage(fieldError.defaultMessage ?: "invalid value", fieldError.field)
            field to message
        }

        log.warn("MethodArgumentNotValidException: {}", fieldErrors, ex)

        // 첫 번째 에러 메시지를 기본 메시지로 사용
        val firstError = fieldErrors.values.firstOrNull() ?: "입력값이 올바르지 않습니다."
        
        return ResponseHelper.error(
            code = "VALIDATION_ERROR",
            message = "입력값 검증에 실패했습니다: $firstError",
            status = HttpStatus.BAD_REQUEST,
            details = fieldErrors
        )
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(
        ex: BindException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate { fieldError ->
            val field = fieldError.field
            val message = getKoreanMessage(fieldError.defaultMessage ?: "invalid value", fieldError.field)
            field to message
        }

        log.warn("BindException: {}", fieldErrors, ex)

        val firstError = fieldErrors.values.firstOrNull() ?: "입력값이 올바르지 않습니다."
        
        return ResponseHelper.error(
            code = "VALIDATION_ERROR",
            message = "입력값 검증에 실패했습니다: $firstError",
            status = HttpStatus.BAD_REQUEST,
            details = fieldErrors
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val fieldErrors = ex.constraintViolations.associate { violation ->
            val field = violation.propertyPath.toString().split(".").lastOrNull() ?: "unknown"
            val message = getKoreanMessage(violation.message, field)
            field to message
        }

        log.warn("ConstraintViolationException: {}", fieldErrors, ex)

        val firstError = fieldErrors.values.firstOrNull() ?: "입력값이 올바르지 않습니다."
        
        return ResponseHelper.error(
            code = "VALIDATION_ERROR",
            message = "입력값 검증에 실패했습니다: $firstError",
            status = HttpStatus.BAD_REQUEST,
            details = fieldErrors
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("IllegalArgumentException: {}", ex.message, ex)
        
        // 메시지로 에러 코드 추론 시도
        val errorCode = when {
            ex.message?.contains("찾을 수 없", ignoreCase = true) == true -> ErrorCode.NOT_FOUND
            ex.message?.contains("권한", ignoreCase = true) == true -> ErrorCode.FORBIDDEN
            ex.message?.contains("인증", ignoreCase = true) == true -> ErrorCode.UNAUTHORIZED
            else -> ErrorCode.INVALID_REQUEST
        }
        
        return ResponseHelper.error(
            code = errorCode.code,
            message = ex.message ?: errorCode.message,
            status = errorCode.httpStatus
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception: {}", ex.javaClass.simpleName, ex)

        return ResponseHelper.error(
            code = ErrorCode.INTERNAL_ERROR.code,
            message = ErrorCode.INTERNAL_ERROR.message,
            status = ErrorCode.INTERNAL_ERROR.httpStatus,
            details = mapOf(
                "exception" to ex.javaClass.simpleName,
                "message" to (ex.message ?: "알 수 없는 오류")
            )
        )
    }

    /**
     * 영어 메시지를 한국어로 변환
     */
    private fun getKoreanMessage(message: String, field: String): String {
        return when {
            message.contains("must not be blank", ignoreCase = true) -> 
                getFieldName(field) + "은(는) 필수 항목입니다."
            message.contains("must not be null", ignoreCase = true) -> 
                getFieldName(field) + "은(는) 필수 항목입니다."
            message.contains("must be a well-formed email address", ignoreCase = true) -> 
                "올바른 이메일 형식이 아닙니다."
            message.contains("size must be between", ignoreCase = true) -> {
                val regex = Regex("size must be between (\\d+) and (\\d+)")
                val match = regex.find(message)
                if (match != null) {
                    val min = match.groupValues[1]
                    val max = match.groupValues[2]
                    "${getFieldName(field)}은(는) ${min}자 이상 ${max}자 이하여야 합니다."
                } else {
                    "${getFieldName(field)}의 길이가 올바르지 않습니다."
                }
            }
            message.contains("must be greater than or equal to", ignoreCase = true) -> {
                val regex = Regex("must be greater than or equal to (\\d+)")
                val match = regex.find(message)
                if (match != null) {
                    val min = match.groupValues[1]
                    "${getFieldName(field)}은(는) ${min} 이상이어야 합니다."
                } else {
                    "${getFieldName(field)}의 값이 너무 작습니다."
                }
            }
            message.contains("must be less than or equal to", ignoreCase = true) -> {
                val regex = Regex("must be less than or equal to (\\d+)")
                val match = regex.find(message)
                if (match != null) {
                    val max = match.groupValues[1]
                    "${getFieldName(field)}은(는) ${max} 이하여야 합니다."
                } else {
                    "${getFieldName(field)}의 값이 너무 큽니다."
                }
            }
            else -> message // 이미 한국어이거나 변환할 수 없는 경우 원본 반환
        }
    }

    /**
     * 필드명을 한국어로 변환
     */
    private fun getFieldName(field: String): String {
        return when (field.lowercase()) {
            "email" -> "이메일"
            "password" -> "비밀번호"
            "username" -> "사용자명"
            "title" -> "제목"
            "content" -> "내용"
            "role" -> "역할"
            "threadid" -> "게시글 ID"
            "parentid" -> "부모 댓글 ID"
            "name" -> "이름"
            "slug" -> "슬러그"
            "description" -> "설명"
            else -> field
        }
    }
}

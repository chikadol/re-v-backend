package com.rev.app.api.error

import org.springframework.http.HttpStatus

/**
 * 비즈니스 로직 예외
 * 
 * 명시적인 에러 코드와 메시지를 가진 커스텀 예외
 */
open class BusinessException(
    val errorCode: ErrorCode,
    message: String? = null,
    val details: Map<String, Any>? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.message, cause) {
    
    constructor(
        errorCode: ErrorCode,
        details: Map<String, Any>? = null,
        cause: Throwable? = null
    ) : this(errorCode, null, details, cause)
}

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
class ResourceNotFoundException(
    errorCode: ErrorCode = ErrorCode.NOT_FOUND,
    message: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, message, details)

/**
 * 인증 관련 예외
 */
class AuthenticationException(
    errorCode: ErrorCode = ErrorCode.UNAUTHORIZED,
    message: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, message, details)

/**
 * 권한 관련 예외
 */
class AuthorizationException(
    errorCode: ErrorCode = ErrorCode.FORBIDDEN,
    message: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, message, details)

/**
 * 검증 실패 예외
 */
class ValidationException(
    errorCode: ErrorCode = ErrorCode.VALIDATION_ERROR,
    message: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, message, details)


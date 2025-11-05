package com.rev.app.api.common

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.validation.BindException

@RestControllerAdvice
class ApiExceptionHandler {

    data class ErrorDetail(val field: String? = null, val message: String)
    data class ApiError(
        val code: String,
        val message: String,
        val errors: List<ErrorDetail> = emptyList()
    )

    private fun badRequest(message: String, errors: List<ErrorDetail> = emptyList())
            : ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(code = "BAD_REQUEST", message = message, errors = errors))

    // ⚠️ IllegalArgumentException 은 단 하나의 핸들러만!
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiError> =
        badRequest(ex.message ?: "Invalid argument")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.map {
            ErrorDetail(field = it.field, message = it.defaultMessage ?: "Invalid value")
        }
        return badRequest("Validation failed", errors)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.map {
            ErrorDetail(field = it.field, message = it.defaultMessage ?: "Invalid value")
        }
        return badRequest("Bind failed", errors)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ApiError> {
        val errors = ex.constraintViolations.map {
            ErrorDetail(field = it.propertyPath?.toString(), message = it.message)
        }
        return badRequest("Constraint violation", errors.toList())
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException): ResponseEntity<ApiError> =
        badRequest("Missing parameter: ${ex.parameterName}")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiError> =
        badRequest("Type mismatch for parameter: ${ex.name}")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiError("METHOD_NOT_ALLOWED", ex.message ?: "Method not allowed"))

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaType(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ApiError("UNSUPPORTED_MEDIA_TYPE", ex.message ?: "Unsupported media type"))

    // 마지막 안전망
    @ExceptionHandler(Exception::class)
    fun handleOthers(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("INTERNAL_ERROR", ex.message ?: "Unexpected error"))
}

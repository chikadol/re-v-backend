// src/main/kotlin/com/rev/app/common/web/GlobalExceptionHandler.kt
package com.rev.app.common.web

import jakarta.validation.ConstraintViolationException
import org.springframework.http.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest

data class ApiError(val code: String, val message: String, val details: Map<String, Any?>? = null)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalid(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val details = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity(ApiError("VALIDATION_ERROR", "Request validation failed", details), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(e: ConstraintViolationException) =
        ResponseEntity(ApiError("CONSTRAINT_VIOLATION", e.message ?: "constraint violation"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException) =
        ResponseEntity(ApiError("NOT_FOUND", e.message ?: "not found"), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBad(e: RuntimeException) =
        ResponseEntity(ApiError("BAD_REQUEST", e.message ?: "bad request"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception, req: WebRequest) =
        ResponseEntity(ApiError("INTERNAL_ERROR", "Unexpected error"), HttpStatus.INTERNAL_SERVER_ERROR)
}

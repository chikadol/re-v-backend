package com.rev.app.api.error

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiErrorResponse> {

        val fieldErrors = ex.bindingResult.fieldErrors.associate { fieldError ->
            val field = fieldError.field
            val message = fieldError.defaultMessage ?: "invalid value"
            field to message
        }

        log.warn("MethodArgumentNotValidException: {}", fieldErrors, ex)

        val body = ApiErrorResponse(
            code = "VALIDATION_ERROR",
            message = "Request validation failed",
            details = fieldErrors
        )

        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(
        ex: BindException
    ): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate { fieldError ->
            val field = fieldError.field
            val message = fieldError.defaultMessage ?: "invalid value"
            field to message
        }

        log.warn("BindException: {}", fieldErrors, ex)

        val body = ApiErrorResponse(
            code = "VALIDATION_ERROR",
            message = "Request validation failed",
            details = fieldErrors
        )

        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.error("Unhandled exception", ex)

        val body = ApiErrorResponse(
            code = "INTERNAL_ERROR",
            message = ex.message ?: "Unexpected error",
            details = mapOf(
                "exception" to ex.javaClass.name
            )
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}

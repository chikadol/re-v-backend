package com.rev.app.api.common

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    data class ErrorBody(val code: String, val message: String?)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorBody("BAD_REQUEST", e.message))

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        BindException::class,
        ConstraintViolationException::class
    )
    fun handleValidation(e: Exception) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorBody("BAD_REQUEST", e.message))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJson(e: HttpMessageNotReadableException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorBody("BAD_REQUEST", e.mostSpecificCause?.message ?: e.message))

    @ExceptionHandler(Exception::class)
    fun handleOthers(e: Exception) =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorBody("INTERNAL_SERVER_ERROR", e.message))
}

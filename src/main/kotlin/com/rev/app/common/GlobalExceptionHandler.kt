package com.rev.app.common


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {
    data class ErrorBody(val code: String, val message: String?)


    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIAE(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody(e.message ?: "BAD_REQUEST", e.message))


    @ExceptionHandler(IllegalStateException::class)
    fun handleISE(e: IllegalStateException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody(e.message ?: "BAD_REQUEST", e.message))


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody("VALIDATION", e.bindingResult.allErrors.firstOrNull()?.defaultMessage))
}
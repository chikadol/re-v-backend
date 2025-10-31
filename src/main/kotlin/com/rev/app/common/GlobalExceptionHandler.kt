// src/main/kotlin/com/rev/app/common/web/GlobalExceptionHandler.kt
package com.rev.app.common.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorRes(val code: String, val message: String)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun handleValidation(ex: Exception): ResponseEntity<ErrorRes> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorRes("VALIDATION_ERROR", ex.message ?: "Invalid request"))

    @ExceptionHandler(IllegalArgumentException::class, NoSuchElementException::class)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ErrorRes> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorRes("BAD_REQUEST", ex.message ?: "Bad request"))

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ErrorRes> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorRes("INTERNAL_ERROR", "Unexpected error"))
}

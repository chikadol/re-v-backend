package com.rev.app.api.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to HttpStatus.BAD_REQUEST.toString(),
                "message" to (ex.message ?: "invalid request")
            )
        )

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun handleValidation(ex: Exception): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to HttpStatus.BAD_REQUEST.toString(),
                "message" to "validation failed"
            )
        )

    @ExceptionHandler(ResponseStatusException::class)
    fun handleRse(ex: ResponseStatusException): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(ex.statusCode).body(
            mapOf(
                "status" to ex.statusCode.value(),        // 400 같은 숫자
                "error" to ex.statusCode.toString(),      // "400 BAD_REQUEST"
                "message" to (ex.reason ?: "")
            )
        )
}

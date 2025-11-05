package com.rev.app.api.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, Any?>> {
        val body = mapOf(
            "error" to "bad_request",
            "message" to ex.message
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}

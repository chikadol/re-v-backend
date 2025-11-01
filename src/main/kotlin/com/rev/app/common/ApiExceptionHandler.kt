package com.rev.app.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val body = ApiError.fromBindingResult(ex.bindingResult)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ApiError> {
        val body = ApiError(ex.reason ?: ex.message ?: "Error")
        return ResponseEntity.status(ex.statusCode).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class, NoSuchElementException::class)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(ex.message ?: "Bad request"))

    @ExceptionHandler(Exception::class)
    fun handleOthers(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("Internal server error"))
}

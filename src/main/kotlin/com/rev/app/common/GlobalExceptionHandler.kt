package com.rev.app.common.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ApiError(val status: Int, val message: String)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException) =
        ResponseEntity(ApiError(404, ex.message ?: "Not Found"), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBadRequest(ex: RuntimeException) =
        ResponseEntity(ApiError(400, ex.message ?: "Bad Request"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun handleValidation(ex: Exception): ResponseEntity<ApiError> {
        val msg = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult.fieldErrors.joinToString { "${it.field}:${it.defaultMessage}" }
            is BindException -> ex.bindingResult.fieldErrors.joinToString { "${it.field}:${it.defaultMessage}" }
            else -> "Validation error"
        }
        return ResponseEntity(ApiError(400, msg), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleOthers(ex: Exception) =
        ResponseEntity(ApiError(500, "Internal error"), HttpStatus.INTERNAL_SERVER_ERROR)
}

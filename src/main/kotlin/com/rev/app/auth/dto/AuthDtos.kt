package com.rev.app.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class SignUpRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 2, max = 50) val username: String,
    @field:Size(min = 8, max = 72) val password: String,
    @field:NotBlank val role: String = "USER" // "USER" or "IDOL"
)


data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)


data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)


data class MeResponse(
    val id: Long,
    val email: String,
    val roles: List<String>,
)
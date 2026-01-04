package com.rev.app.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class SignUpRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일은 필수 항목입니다.")
    val email: String,
    
    @field:NotBlank(message = "사용자명은 필수 항목입니다.")
    @field:Size(min = 2, max = 50, message = "사용자명은 2자 이상 50자 이하여야 합니다.")
    val username: String,
    
    @field:NotBlank(message = "비밀번호는 필수 항목입니다.")
    @field:Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
    val password: String,
    
    @field:NotBlank(message = "역할은 필수 항목입니다.")
    val role: String = "USER" // "USER" or "IDOL"
)


data class LoginRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일은 필수 항목입니다.")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 항목입니다.")
    val password: String,
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
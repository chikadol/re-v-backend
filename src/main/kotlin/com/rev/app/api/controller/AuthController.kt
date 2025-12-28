package com.rev.app.api.controller

import com.rev.app.auth.AuthService
import com.rev.app.auth.TokenResponse
import com.rev.app.auth.jwt.JwtProvider
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/auth")
abstract class AuthController {
    abstract val authService: AuthService
    abstract val jwtProvider: JwtProvider
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): TokenResponse? {
        val token = authService.loginByEmail(req.email, req.password)
        return token?.let {
            TokenResponse(
                it
            )
        }
    }
    @PostMapping("/refresh")
    fun refresh(@RequestHeader("Authorization") token: String): com.rev.app.auth.dto.TokenResponse {
        val refreshToken = token.removePrefix("Bearer ")
        return authService.refresh(refreshToken)
    }

    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class LoginResponse(
        val accessToken: String,
        val refreshToken: String
    )

}
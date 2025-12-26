package com.rev.app.api.controller

import com.rev.app.auth.AuthService
import com.rev.app.auth.jwt.JwtProvider
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
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
    fun login(@RequestBody req: LoginRequest): LoginResponse {
        // 실제 로그인 로직은 DB 기반으로 나중에 구현할 것.
        // 일단은 테스트용으로 무조건 성공 처리.

        val fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val accessToken = jwtProvider.generateAccessToken(fakeUserId, req.email, listOf("USER"))
        val refreshToken = jwtProvider.generateRefreshToken(fakeUserId, req.email, listOf("USER"))

        return LoginResponse(accessToken, refreshToken)
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
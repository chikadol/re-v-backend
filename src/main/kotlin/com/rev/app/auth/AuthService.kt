package com.rev.app.auth

import com.rev.app.auth.dto.TokenResponse
import com.rev.app.auth.jwt.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun loginByEmail(email: String, password: String): TokenResponse? {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid credentials")

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        return user.id?.let { userId ->
            TokenResponse(
                accessToken = jwtProvider.generateAccessToken(userId),
                refreshToken = jwtProvider.generateRefreshToken(userId)
            )
        }
    }

    fun login(userId: UUID): TokenResponse {
        val access = jwtProvider.generateAccessToken(userId)
        val refresh = jwtProvider.generateRefreshToken(userId)

        return TokenResponse(access, refresh)
    }

    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtProvider.validate(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtProvider.getUserId(refreshToken)

        return TokenResponse(
            accessToken = jwtProvider.generateAccessToken(userId),
            refreshToken = jwtProvider.generateRefreshToken(userId)
        )
    }

    private inline fun <reified T> tryGet(target: Any, methodName: String): T? =
        try { target::class.java.getMethod(methodName).invoke(target) as? T }
        catch (_: Exception) { null }
}



data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)

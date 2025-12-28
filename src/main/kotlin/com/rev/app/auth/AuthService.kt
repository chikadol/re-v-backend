package com.rev.app.auth

import com.rev.app.auth.dto.TokenResponse
import com.rev.app.auth.jwt.JwtProvider
import org.springframework.stereotype.Service
import java.util.*

    @Service
    class AuthService(
        private val userRepository: UserRepository,
        private val jwtProvider: JwtProvider
    ) {
        fun loginByEmail(email: String, password: String): String? {
            val user = userRepository.findByEmail(email)
                ?: throw IllegalArgumentException("Invalid credentials")

            return user.id?.let { jwtProvider.generateAccessToken(it) }
        }

        fun login(userId: UUID): com.rev.app.auth.dto.TokenResponse {
            val access = jwtProvider.generateAccessToken(userId)
            val refresh = jwtProvider.generateRefreshToken(userId)

            return com.rev.app.auth.dto.TokenResponse(access, refresh)
        }

        fun refresh(refreshToken: String): TokenResponse {
            if (!jwtProvider.validate(refreshToken)) {
                throw IllegalArgumentException("Invalid refresh token")
            }

            val userId = jwtProvider.getUserId(refreshToken)

            return com.rev.app.auth.dto.TokenResponse(
                jwtProvider.generateAccessToken(userId),
                jwtProvider.generateRefreshToken(userId)
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

package com.rev.app.auth

import com.rev.app.auth.jwt.JwtProvider
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtProvider: JwtProvider
) {
    fun login(username: String, roles: List<String>): TokenPair {
        val access = jwtProvider.generateAccessToken(username, roles)
        val refresh = jwtProvider.generateRefreshToken(username)
        return TokenPair(access, refresh)
    }

    fun refresh(refreshToken: String): TokenPair {
        // 1) 토큰 검증 + 리프레시 여부 확인
        require(jwtProvider.validate(refreshToken) && jwtProvider.isRefresh(refreshToken)) {
            "not a refresh token"
        }

        // 2) 사용자 / 역할 추출 (리프레시 토큰에 roles 없으면 기본 ROLE_USER)
        val username = jwtProvider.parseSubject(refreshToken)
        val roles = jwtProvider.parseRoles(refreshToken).ifEmpty { listOf("ROLE_USER") }

        // 3) 새 토큰 발급
        val newAccess = jwtProvider.generateAccessToken(username, roles)
        val newRefresh = jwtProvider.generateRefreshToken(username)

        return TokenPair(newAccess, newRefresh)
    }
}

data class TokenPair(val accessToken: String, val refreshToken: String)

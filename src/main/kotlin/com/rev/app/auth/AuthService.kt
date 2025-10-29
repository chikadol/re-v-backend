package com.rev.app.auth

import com.rev.app.auth.entity.RefreshToken
import com.rev.app.auth.jwt.JwtProvider
import com.rev.app.auth.repo.RefreshTokenRepo
import com.rev.app.auth.repo.hashToken
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepo: RefreshTokenRepo
) {
    fun login(username: String, roles: List<String>): TokenPair {
        val access = jwtProvider.generateAccessToken(username, roles)
        val refresh = jwtProvider.generateRefreshToken(username)
        refreshTokenRepo.save(
            RefreshToken(
                subject = username,
                tokenHash = hashToken(refresh)
//                expiresAt = jwtProvider.getExpiration(refresh)
            )
        )
        return TokenPair(access, refresh)
    }

    fun refresh(refreshToken: String): TokenPair {
        require(jwtProvider.validate(refreshToken) && jwtProvider.isRefresh(refreshToken)) { "not a refresh token" }

        val h = hashToken(refreshToken)
        val row = refreshTokenRepo.findByTokenHash(h).orElseThrow { IllegalArgumentException("unknown refresh") }
        require(!row.revoked && row.expiresAt.isAfter(Instant.now())) { "refresh expired/revoked" }

        row.revoked = true
        refreshTokenRepo.save(row)

        val subject = jwtProvider.parseSubject(refreshToken)
        val newAccess = jwtProvider.generateAccessToken(subject, emptyList())
        val newRefresh = jwtProvider.generateRefreshToken(subject)
        refreshTokenRepo.save(
            RefreshToken(subject = subject, tokenHash = hashToken(newRefresh)/*, expiresAt = jwtProvider.getExpiration(newRefresh)*/)
        )
        return TokenPair(newAccess, newRefresh)
    }
}

data class TokenPair(val accessToken: String, val refreshToken: String)

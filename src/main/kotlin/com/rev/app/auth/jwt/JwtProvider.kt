package com.rev.app.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(

    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,

    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long
) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateAccessToken(userId: UUID): String {
        return generateToken(userId, accessTokenExpiration)
    }

    fun generateRefreshToken(userId: UUID): String {
        return generateToken(userId, refreshTokenExpiration)
    }

    private fun generateToken(userId: UUID, expiration: Long): String {
        val now = Date()
        val exp = Date(now.time + expiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validate(token: String): Boolean =
        try {
            parse(token)
            true
        } catch (e: Exception) {
            false
        }

    fun getUserId(token: String): UUID =
        UUID.fromString(parse(token).subject)

    private fun parse(token: String) =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
}

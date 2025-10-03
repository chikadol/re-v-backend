package com.rev.app.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

@Service
class JwtService(
    @Value("\${app.jwt.issuer}") private val issuer: String,
    @Value("\${app.jwt.accessSecret}") private val accessSecret: String,
    @Value("\${app.jwt.refreshSecret}") private val refreshSecret: String,
    @Value("\${app.jwt.accessTtlSeconds}") private val accessTtl: Long,
    @Value("\${app.jwt.refreshTtlSeconds}") private val refreshTtl: Long
) {
    fun createAccessToken(userId: Long): String = buildToken(userId, accessSecret, accessTtl)
    fun createRefreshToken(userId: Long): String = buildToken(userId, refreshSecret, refreshTtl)

    fun parseAccess(token: String): Long? = parseUserId(token, accessSecret)
    fun parseRefresh(token: String): Long? = parseUserId(token, refreshSecret)

    private fun buildToken(userId: Long, secret: String, ttl: Long): String {
        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
        return Jwts.builder()
            .issuer(issuer)
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(ttl)))
            .signWith(key)
            .compact()
    }

    private fun parseUserId(token: String, secret: String): Long? = try {
        val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        claims.subject.toLong()
    } catch (e: Exception) { null }
}

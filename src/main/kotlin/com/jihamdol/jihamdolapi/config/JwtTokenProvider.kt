package com.jihamdol.jihamdolapi.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKeyBase64: String,
    @Value("\${jwt.expiration-in-ms}") private val validityInMilliseconds: Long
) {
    private lateinit var key: Key

    @PostConstruct
    fun init() {
        val keyBytes = Decoders.BASE64.decode(secretKeyBase64)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    fun createToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsername(token: String): String {
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims: Claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun createRefreshToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + (validityInMilliseconds * 7)) // 7일
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

}

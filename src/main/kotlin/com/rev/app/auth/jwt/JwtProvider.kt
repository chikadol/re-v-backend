package com.rev.app.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import org.springframework.stereotype.Component


@Component
class JwtProvider(private val props: JwtProps) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(props.secret.toByteArray()) }


    fun generateAccessToken(subject: String, roles: List<String>): String {
        val now = Instant.now()
        val exp = now.plusSeconds(props.accessTokenMinutes * 60)
        return Jwts.builder()
            .setSubject(subject)
            .setIssuer(props.issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .claim("roles", roles)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }


    fun generateRefreshToken(subject: String): String {
        val now = Instant.now()
        val exp = now.plusSeconds(props.refreshTokenDays * 24 * 3600)
        return Jwts.builder()
            .setSubject(subject)
            .setIssuer(props.issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }


    fun parseSubject(token: String): String? = try {
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject
    } catch (e: Exception) { null }


    fun parseRoles(token: String): List<String> = try {
        val claims = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body
        @Suppress("UNCHECKED_CAST")
        (claims["roles"] as? List<String>) ?: emptyList()
    } catch (_: Exception) { emptyList() }
}
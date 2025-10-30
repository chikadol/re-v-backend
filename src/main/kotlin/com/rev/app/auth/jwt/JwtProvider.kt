package com.rev.app.auth.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(private val props: JwtProperties) {

    private val key: SecretKey = run {
        val sec = props.secret.trim()
        require(sec.length >= 32) { "jwt.secret must be >= 32 chars" }
        Keys.hmacShaKeyFor(sec.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateAccessToken(subject: String, roles: List<String>): String =
        Jwts.builder()
            .setSubject(subject)
            .claim("roles", roles)
            .setIssuer(props.issuer)
            .setExpiration(Date.from(Instant.now().plusSeconds(props.accessTtlSeconds)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

    fun generateRefreshToken(subject: String): String =
        Jwts.builder()
            .setSubject(subject)
            .claim("typ", "refresh")          // 리프레시 표식
            .setIssuer(props.issuer)
            .setExpiration(Date.from(Instant.now().plusSeconds(props.refreshTtlSeconds)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

    fun validate(token: String): Boolean = try {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (_: JwtException) { false }      // Signature/Expired/Malformed 등
    catch (_: IllegalArgumentException) { false }

    fun isRefresh(token: String): Boolean = try {
        val body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        body["typ"] == "refresh"
    } catch (_: Exception) { false }

    fun parseSubject(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body.subject

    fun parseRoles(token: String): List<String> {
        val body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        val raw = body["roles"]
        return when (raw) {
            is Collection<*> -> raw.filterIsInstance<String>()
            is String -> raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }

    fun accessTtlSeconds(): Long = props.accessTtlSeconds
    fun refreshTtlSeconds(): Long = props.refreshTtlSeconds
}

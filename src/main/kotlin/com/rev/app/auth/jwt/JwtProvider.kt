package com.rev.app.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val props: JwtProperties
) {
    private val key: SecretKey =
        Keys.hmacShaKeyFor(props.secret.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(subject: String, roles: List<String>): String =
        Jwts.builder()
            .setSubject(subject)
            .claim("roles", roles)
            .claim("type", "access")
            .setIssuer(props.issuer)
            .setExpiration(Date(System.currentTimeMillis() + props.accessTtlSeconds * 1000))
            .signWith(key)
            .compact()

    fun generateRefreshToken(subject: String): String =
        Jwts.builder()
            .setSubject(subject)
            .claim("type", "refresh")
            .setIssuer(props.issuer)
            .setExpiration(Date(System.currentTimeMillis() + props.refreshTtlSeconds * 1000))
            .signWith(key)
            .compact()

    /** ✅ 여기 때문에 AuthService의 validate 호출이 컴파일됩니다 */
    fun validate(token: String): Boolean = try {
        // jjwt 0.11.x: parserBuilder() + setSigningKey(...)
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (_: Exception) {
        false
    }

    fun isRefresh(token: String): Boolean {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return (claims["type"] as? String) == "refresh"
    }

    fun parseSubject(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body.subject

    fun parseRoles(token: String): List<String> {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        val raw = claims["roles"]
        return when (raw) {
            is Collection<*> -> raw.filterIsInstance<String>()
            is String -> raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }
/*    fun getExpiration(token: String): Instant {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return claims.expiration.toInstant()
    }*/

}

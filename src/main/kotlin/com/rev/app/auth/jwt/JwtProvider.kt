package com.rev.app.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val props: JwtProperties
) {
    private val key: SecretKey = run {
        require(props.secret.length >= 32) { "jwt.secret must be >= 32 chars" }
        Keys.hmacShaKeyFor(props.secret.toByteArray(Charsets.UTF_8))
    }

    fun resolveToken(request: HttpServletRequest): String? {
        val hv = request.getHeader(props.header) ?: return null
        val prefix = props.prefix
        return if (hv.startsWith(prefix, true)) hv.substring(prefix.length).trim() else null
    }

    fun validate(token: String): Boolean = try {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (_: Exception) { false }

    fun getSubject(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject

    fun getUserId(token: String): UUID = UUID.fromString(getSubject(token))

    fun getRoles(token: String): List<String> {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return when (val raw = claims["roles"]) {
            is Collection<*> -> raw.filterIsInstance<String>()
            is Array<*>       -> raw.filterIsInstance<String>()
            is String         -> raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            else              -> emptyList()
        }
    }

    fun isRefresh(token: String): Boolean {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return (claims["typ"] as? String)?.equals("refresh", true) == true
    }

    fun generateAccessToken(userId: UUID, username: String, roles: Collection<String> = emptyList()): String {
        val now = Date()
        val exp = Date(now.time + props.accessTtlSeconds * 1000)
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("un", username)
            .claim("roles", roles)
            .claim("typ", "access")
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val exp = Date(now.time + props.refreshTtlSeconds * 1000)
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("typ", "refresh")
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
}

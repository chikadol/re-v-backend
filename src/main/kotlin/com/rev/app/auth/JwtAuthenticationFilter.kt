package com.rev.app.auth

import com.rev.app.auth.jwt.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@ConditionalOnProperty(name = ["auth.jwt.enabled"], havingValue = "true", matchIfMissing = true)
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider   // JwtService가 아니라 JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtProvider.resolveToken(request)
        if (token != null && jwtProvider.validate(token)) {
            val userId = jwtProvider.getUserId(token)
            val roles  = jwtProvider.getRoles(token)
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.substring(7) else null
    }
}

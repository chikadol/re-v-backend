package com.rev.app.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwt: JwtService): OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        // Extract from Authorization: Bearer <token>
        val auth = req.getHeader("Authorization")
        if (auth != null && auth.startsWith("Bearer ")) {
            val token = auth.substring(7)
            val userId = jwt.parseAccess(token)
            if (userId != null) {
                req.setAttribute("authPrincipal", AuthPrincipal(userId))
            }
        }
        chain.doFilter(req, res)
    }
}

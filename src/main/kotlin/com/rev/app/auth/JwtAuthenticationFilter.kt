package com.rev.app.auth

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.jwt.JwtProvider
import com.rev.app.auth.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)
        
        logger.debug("JWT 필터 실행: path=${request.requestURI}, token 존재=${token != null}")

        if (token != null && jwtProvider.validate(token)) {
            val userId = jwtProvider.getUserId(token)
            logger.debug("JWT 토큰 검증 성공: userId=$userId")
            
            // 사용자 정보 조회하여 email 가져오기
            val user = userRepository.findById(userId).orElse(null)
            val email = user?.email ?: ""
            
            logger.debug("사용자 정보 조회: userId=$userId, email=$email")

            val principal = JwtPrincipal(
                userId = userId,
                email = email,
                roles = emptyList()
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,   // principal
                null,        // credentials
                emptyList()  // authorities (지금은 없음)
            )

            SecurityContextHolder.getContext().authentication = authentication
            logger.debug("SecurityContext에 인증 정보 설정 완료: userId=$userId")
        } else {
            logger.debug("JWT 토큰 없음 또는 유효하지 않음: path=${request.requestURI}")
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        return if (bearer != null && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else null
    }
}

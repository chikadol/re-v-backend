package com.rev.app.api.security

import com.rev.app.auth.CustomUserPrincipal
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.auth.jwt.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

@Component
class MeArgumentResolver(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(Me::class.java)
                && parameter.parameterType.isAssignableFrom(UserEntity::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        // 1) SecurityContext
        SecurityContextHolder.getContext().authentication?.principal?.let { p ->
            if (p is CustomUserPrincipal) {
                return userRepository.findById(p.userId)
                    .orElseThrow { IllegalStateException("Current user not found: ${p.userId}") }
            }
        }

        // 2) JWT 직접 파싱
        val req = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("No HttpServletRequest")
        val token = jwtProvider.resolveToken(req) ?: throw IllegalStateException("No JWT token")
        if (!jwtProvider.validate(token)) throw IllegalStateException("Invalid token")

        val uid: UUID = jwtProvider.getUserId(token)
        return userRepository.findById(uid)
            .orElseThrow { IllegalStateException("Current user not found: $uid") }
    }
}

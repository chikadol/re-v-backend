package com.rev.app.api.security

import com.rev.app.auth.jwt.JwtProvider
import com.rev.app.auth.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class MeArgumentResolver(
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(Me::class.java)
                && parameter.parameterType == MeDto::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("Unauthenticated")
        val username = auth.name
        val user = userRepository.findByUsername(username)
            ?: throw IllegalStateException("User not found: $username")

        val roles = auth.authorities.map { it.authority }   // ← 여기가 String.map(...)로 잘못되면 모호성 에러뜸

        return MeDto(
            userId = user.id!!,
            username = user.username,
            roles = roles
        )
    }
}

    /**
     * roles가 컬렉션/배열/문자열 등 무엇으로 오든 안전하게 List<String>으로 변환.
     * CharSequence.map 과 Iterable.map의 모호성을 제거하기 위해 명시적 캐스팅 사용.
     */
    private fun toRoleStrings(src: Any?): List<String> = when (src) {
        null -> emptyList()
        is Collection<*> -> src.map { it.toString() }
        is Array<*> -> src.map { it.toString() }
        is CharSequence -> src.toString()
            .split(',', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        else -> listOf(src.toString())
    }

package com.rev.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

class PermissivePrincipalResolver(private val uid: UUID) : HandlerMethodArgumentResolver {
    private val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    override fun supportsParameter(p: org.springframework.core.MethodParameter): Boolean {
        if (p.hasParameterAnnotation(AuthenticationPrincipal::class.java)) return true
        val n = p.parameterType.simpleName.lowercase()
        return n.contains("jwt") && n.contains("principal")
    }
    override fun resolveArgument(
        p: org.springframework.core.MethodParameter,
        mav: ModelAndViewContainer?, req: NativeWebRequest, bf: WebDataBinderFactory?
    ): Any = mapper.convertValue(
        mapOf("userId" to uid, "email" to "mock@test.com", "roles" to listOf("USER")),
        p.parameterType
    )
}

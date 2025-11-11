package com.rev.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

@Suppress("UNCHECKED_CAST")
fun <T> anyK(clazz: Class<T>): T = ArgumentMatchers.any(clazz)
fun <T> eqK(value: T): T = ArgumentMatchers.eq(value)
fun <T> anyListK(): MutableList<T> = ArgumentMatchers.anyList<T>() as MutableList<T>

fun <T> lenientReturn(value: T) = Mockito.lenient().doReturn(value)

fun <T> emptyPage(): Page<T> = PageImpl(emptyList(), PageRequest.of(0, 10), 0)

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

fun standaloneMvc(controller: Any, principalUid: UUID? = null): MockMvc {
    val om = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    val builder = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(MappingJackson2HttpMessageConverter(om))
    if (principalUid != null) {
        builder.setCustomArgumentResolvers(
            PageableHandlerMethodArgumentResolver(),
            PermissivePrincipalResolver(principalUid)
        )
    } else {
        builder.setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
    }
    return builder.build()
}

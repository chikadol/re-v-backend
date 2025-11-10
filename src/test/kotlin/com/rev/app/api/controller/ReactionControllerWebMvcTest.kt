package com.rev.app.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rev.app.api.service.community.ReactionService
import com.rev.app.api.service.community.dto.ToggleReactionRes
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

class ReactionControllerWebMvcTest {

    private val service: ReactionService = Mockito.mock(ReactionService::class.java)

    companion object {
        private val FIXED_UID: UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111")
    }

    /** @AuthenticationPrincipal(JwtPrincipal) 를 Jackson으로 생성해 주입 */
    private class JacksonAuthPrincipalResolver(
        private val fixedUserId: UUID
    ) : HandlerMethodArgumentResolver {
        private val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

        override fun supportsParameter(p: org.springframework.core.MethodParameter) =
            p.hasParameterAnnotation(AuthenticationPrincipal::class.java)

        override fun resolveArgument(
            p: org.springframework.core.MethodParameter,
            mav: ModelAndViewContainer?,
            req: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any {
            val payload = mapOf(
                "userId" to fixedUserId,
                "email" to "mock@test.com",
                "roles" to listOf("USER")
            )
            return mapper.convertValue(payload, p.parameterType)
        }
    }

    private fun mockMvcFor(controller: Any): MockMvc {
        val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        return MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                JacksonAuthPrincipalResolver(FIXED_UID)
            )
            .setMessageConverters(MappingJackson2HttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun toggle_ok() {
        val controller = ReactionController(service) // ← 실제 패키지 import 확인
        val mockMvc = mockMvcFor(controller)

        val threadId = UUID.randomUUID()
        val type = "LIKE"

        // 서비스 스텁: toggle(userId, threadId, type)
        Mockito.`when`(
            service.toggle(
                ArgumentMatchers.eq(FIXED_UID),
                ArgumentMatchers.eq(threadId),
                ArgumentMatchers.eq(type)
            )
        ).thenReturn(ToggleReactionRes(toggled = true, counts = mapOf("LIKE" to 1L, "LOVE" to 0L)))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/threads/$threadId/reactions/$type")
                .accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }
}

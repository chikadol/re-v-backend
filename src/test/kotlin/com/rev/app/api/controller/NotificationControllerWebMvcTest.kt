package com.rev.app.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
import java.time.Instant
import java.util.UUID

class NotificationControllerWebMvcTest {

    private val service: NotificationService = Mockito.mock(NotificationService::class.java)

    companion object {
        private val FIXED_UID: UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111")
    }

    /** JwtPrincipal 생성자 모양과 무관하게 Jackson으로 변환해 주입 */
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
    fun listMine_ok() {
        val controller = NotificationController(service)
        val mockMvc = mockMvcFor(controller)

        val notif = NotificationRes(
            id = UUID.randomUUID(),
            type = "COMMENT",
            threadId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            message = "새 댓글: 테스트",
            isRead = false,
            createdAt = Instant.now()
        )

        Mockito.`when`(
            service.listMine(
                ArgumentMatchers.eq(FIXED_UID),
                ArgumentMatchers.any(Pageable::class.java)
            )
        ).thenReturn(PageImpl(listOf(notif)))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notifications")
                .accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }

    @Test
    fun markRead_ok() {
        val controller = NotificationController(service)
        val mockMvc = mockMvcFor(controller)

        val notif = NotificationRes(
            id = UUID.randomUUID(),
            type = "COMMENT",
            threadId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            message = "테스트",
            isRead = true,
            createdAt = Instant.now()
        )

        Mockito.`when`(
            service.markRead(
                ArgumentMatchers.eq(FIXED_UID),
                ArgumentMatchers.eq(notif.id)
            )
        ).thenReturn(notif)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/notifications/${notif.id}/read")
                .accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }
}

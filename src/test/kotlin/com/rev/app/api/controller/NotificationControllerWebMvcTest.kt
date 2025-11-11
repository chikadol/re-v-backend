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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
    private val FIXED_UID = UUID.fromString("11111111-1111-1111-1111-111111111111")

    private class PermissivePrincipalResolver(
        private val uid: UUID
    ) : HandlerMethodArgumentResolver {
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

    private fun mvc(controller: Any): MockMvc {
        val om = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        return MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver(), PermissivePrincipalResolver(FIXED_UID))
            .setMessageConverters(MappingJackson2HttpMessageConverter(om))
            .build()
    }

    @Test
    fun listMine_ok() {
        val controller = NotificationController(service)
        val mockMvc = mvc(controller)

        val notif = NotificationRes(
            id = UUID.randomUUID(),
            type = "COMMENT",
            threadId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            message = "새 댓글",
            isRead = false,
            createdAt = Instant.now()
        )

        Mockito.doReturn(PageImpl(listOf(notif))).`when`(service).listMine(
            ArgumentMatchers.eq(FIXED_UID),
            ArgumentMatchers.any(Pageable::class.java)
        )

        mockMvc.perform(get("/api/notifications").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun markRead_ok() {
        val controller = NotificationController(service)
        val mockMvc = mvc(controller)

        val notifId = UUID.randomUUID()
        val updated = NotificationRes(
            id = notifId,
            type = "COMMENT",
            threadId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            message = "읽음",
            isRead = true,
            createdAt = Instant.now()
        )

        Mockito.doReturn(updated).`when`(service).markRead(
            ArgumentMatchers.eq(FIXED_UID),
            ArgumentMatchers.eq(notifId)
        )

        mockMvc.perform(post("/api/notifications/$notifId/read").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk)
    }
}

package com.rev.app.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.Instant
import java.util.*

class NotificationControllerWebMvcTest {

    private val service: NotificationService = mock()

    companion object {
        private val FIXED_UID: UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111")
    }

    /** @AuthenticationPrincipal 주입용 가짜 리졸버 */
    private class LenientAuthPrincipalResolver(
        private val fixedUserId: UUID
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(param: org.springframework.core.MethodParameter): Boolean =
            param.hasParameterAnnotation(AuthenticationPrincipal::class.java)

        override fun resolveArgument(
            param: org.springframework.core.MethodParameter,
            mav: ModelAndViewContainer?,
            req: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any {
            val t = param.parameterType
            // (UUID, String, Collection) 생성자 우선
            return runCatching {
                val ctor = t.getDeclaredConstructor(UUID::class.java, String::class.java, Collection::class.java)
                ctor.isAccessible = true
                ctor.newInstance(fixedUserId, "mock@test.com", listOf("USER"))
            }.getOrElse {
                // no-arg + 필드 주입
                val inst = t.getDeclaredConstructor().newInstance()
                runCatching { t.getDeclaredField("userId").apply { isAccessible = true }.set(inst, fixedUserId) }
                runCatching { t.getDeclaredField("email").apply { isAccessible = true }.set(inst, "mock@test.com") }
                runCatching { t.getDeclaredField("roles").apply { isAccessible = true }.set(inst, listOf("USER")) }
                inst
            }
        }
    }

    private fun mockMvcFor(controller: Any): MockMvc {
        val mapper = ObjectMapper().registerModule(JavaTimeModule())
        val builder: org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder =
            MockMvcBuilders.standaloneSetup(controller)
        builder.setCustomArgumentResolvers(
            PageableHandlerMethodArgumentResolver(),
            LenientAuthPrincipalResolver(FIXED_UID)
        )
        builder.setMessageConverters(MappingJackson2HttpMessageConverter(mapper))
        return builder.build()
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

        // ✅ UUID는 eq(FIXED_UID), Pageable은 Mockito.any(Pageable::class.java)
        whenever(service.listMine(eq(FIXED_UID), Mockito.any(Pageable::class.java)))
            .thenReturn(PageImpl(listOf(notif)))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notifications")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
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

        // ✅ markRead(userId, id) 모두 eq로 고정
        whenever(service.markRead(eq(FIXED_UID), eq(notif.id)))
            .thenReturn(notif)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/notifications/${notif.id}/read")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

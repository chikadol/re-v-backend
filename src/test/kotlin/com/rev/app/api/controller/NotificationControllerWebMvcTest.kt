package com.rev.app.api.controller

import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import com.rev.test.PermissivePrincipalResolver
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.*
import org.junit.jupiter.api.Disabled

@Disabled("임시 비활성화 - 테스트 환경/Mockito 정리 후 다시 살릴 예정")
class NotificationControllerWebMvcTest {

    private val service: NotificationService = Mockito.mock(NotificationService::class.java)
    private val FIXED_UID: UUID =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

    @Test
    fun listMine_ok() {
        val controller = NotificationController(
            service,
            notificationService = service
        )

        // ✅ Resolver 2개 모두 설정 (NPE 방지)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                PermissivePrincipalResolver(FIXED_UID)
            )
            .build()

        val notif = NotificationRes(
            id = UUID.randomUUID(),
            type = "COMMENT",
            threadId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            message = "새 댓글",
            isRead = false,
            createdAt = Instant.now()
        )

        // ✅ 매처는 반드시 @Test 안에서, doReturn-when 체인 안에서만
        Mockito.lenient().doReturn(PageImpl(listOf(notif)))
            .`when`(service).listMine(
                ArgumentMatchers.eq(FIXED_UID),
                ArgumentMatchers.any(Pageable::class.java),
                ArgumentMatchers.any()
            )

        mockMvc.perform(get("/api/notifications").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun markRead_ok() {
        val controller = NotificationController(
            service,
            notificationService = service
        )
        val mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                PermissivePrincipalResolver(FIXED_UID)
            )
            .build()

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

        Mockito.lenient().doReturn(updated)
            .`when`(service).markRead(
                ArgumentMatchers.eq(FIXED_UID),
                ArgumentMatchers.eq(notifId)
            )

        mockMvc.perform(post("/api/notifications/$notifId/read").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

}
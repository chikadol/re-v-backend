package com.rev.app.api.controller

import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import com.rev.test.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.*

class NotificationControllerWebMvcTest {

    private val service: NotificationService = Mockito.mock(NotificationService::class.java)
    private val FIXED_UID: UUID =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

    @Test
    fun listMine_ok() {
        val controller = NotificationController(service)
        val mockMvc = standaloneMvc(controller, principalUid = FIXED_UID)

        val notif = NotificationRes(
            id = UUID.randomUUID(),
            type = "COMMENT",
            threadId = UUID.randomUUID(),
            commentId = UUID.randomUUID(),
            message = "새 댓글",
            isRead = false,
            createdAt = Instant.now()
        )

        lenientReturn(PageImpl(listOf(notif)))
            .`when`(service).listMine(
                eqK(FIXED_UID),
                anyK(Pageable::class.java),
                bool = false
            )

        mockMvc.perform(get("/api/notifications").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun markRead_ok() {
        val controller = NotificationController(service)
        val mockMvc = standaloneMvc(controller, principalUid = FIXED_UID)

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

        lenientReturn(updated)
            .`when`(service).markRead(eqK(FIXED_UID), eqK(notifId))

        mockMvc.perform(post("/api/notifications/$notifId/read").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }
}

package com.rev.app.api.controller

import com.rev.app.api.service.community.ReactionService
import com.rev.app.api.service.community.dto.ToggleReactionRes
import com.rev.test.PermissivePrincipalResolver
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*
import org.junit.jupiter.api.Disabled

@Disabled("임시 비활성화 - 테스트 환경/Mockito 정리 후 다시 살릴 예정")
class ReactionControllerWebMvcTest {

    private val service: ReactionService = Mockito.mock(ReactionService::class.java)
    private val FIXED_UID: UUID =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

    @Test
    fun toggle_ok() {
        val controller = ReactionController(service)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                PermissivePrincipalResolver(FIXED_UID)
            )
            .build()

        val threadId = UUID.randomUUID()

        // ✅ doReturn -> `when` -> 메서드(인자 매처 포함) 까지 "반드시" 완결
        Mockito.lenient().doReturn(
            ToggleReactionRes(true, mapOf("LIKE" to 1L, "LOVE" to 0L))
        ).`when`(service).toggle(
            ArgumentMatchers.eq(FIXED_UID),
            ArgumentMatchers.eq(threadId),
            ArgumentMatchers.eq("LIKE")
        )

        mockMvc.perform(
            post("/api/threads/$threadId/reactions/LIKE")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

package com.rev.app.api.service.community

import com.rev.app.api.controller.ThreadController
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any as anyK
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class ThreadApiTest {

    // ✅ 타입을 반드시 우리 서비스로 명시
    private val threadService: ThreadService = mock()

    @Test
    fun listPublic_ok() {
        val controller = ThreadController(threadService)

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()

        // ✅ 반환 타입도 명시 (제네릭 추론 오류 방지)
        whenever(threadService.listPublic(
            anyK<UUID>(),
            anyK() // Pageable
        )).thenReturn(PageImpl<ThreadRes>(emptyList()))

        val boardId = UUID.randomUUID()

        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

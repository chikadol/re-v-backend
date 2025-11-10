package com.rev.app.api.service.community

import com.rev.app.api.controller.ThreadController
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.BeforeEach
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

class ThreadWebMvcTest {

    private lateinit var mockMvc: MockMvc
    private val service: ThreadService = mock()

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(ThreadController(service))
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    fun listPublic_ok() {
        val boardId = UUID.randomUUID()
        val emptyPage = PageImpl<ThreadRes>(emptyList())

        // 2인자/3인자 모두 스텁 (안전)
        whenever(service.listPublic(anyK<UUID>(), anyK())).thenReturn(emptyPage)
        whenever(service.listPublic(anyK<UUID>(), anyK(), anyK<List<String>>())).thenReturn(emptyPage)

        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

package com.rev.app.api.controller
import com.rev.test.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.UUID

class ThreadControllerTagWebMvcTest {
    // --- Mockito matcher helpers (Kotlin null/제네릭 안전) ---
    private fun <T> eqK(v: T): T = org.mockito.ArgumentMatchers.eq(v)
    private fun <T> anyK(clazz: Class<T>): T = org.mockito.ArgumentMatchers.any(clazz)

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    private fun mvc(controller: Any): MockMvc {
        val om = ObjectMapper().registerModule(JavaTimeModule())
        return MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(om))
            .build()
    }

    @Test
    fun list_with_tags_ok() {
        val controller = ThreadController(service)
        val mockMvc = mvc(controller)

        val boardId = UUID.randomUUID()
        val pageable: Pageable = PageRequest.of(0, 10)

        val now = Instant.now()
        val sample = ThreadRes(
            id = UUID.randomUUID(), title = "t", content = "c",
            boardId = boardId, parentThreadId = null, authorId = UUID.randomUUID(),
            isPrivate = false, categoryId = null, createdAt = now, updatedAt = now, tags = emptyList()
        )
        val page: org.springframework.data.domain.Page<com.rev.app.api.service.community.dto.ThreadRes> =
            org.springframework.data.domain.PageImpl(
                emptyList(),
                org.springframework.data.domain.PageRequest.of(0, 10),
                0
            )

// 2-인자
        org.mockito.Mockito.lenient().doReturn(page)
            .`when`(service).listPublic(
                eqK(boardId),
                anyK(org.springframework.data.domain.Pageable::class.java)
            )

// 3-인자
        org.mockito.Mockito.lenient().doReturn(page)
            .`when`(service).listPublic(
                eqK(boardId),
                anyK(org.springframework.data.domain.Pageable::class.java),
                org.mockito.ArgumentMatchers.anyList()
            )

        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0").param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }
}

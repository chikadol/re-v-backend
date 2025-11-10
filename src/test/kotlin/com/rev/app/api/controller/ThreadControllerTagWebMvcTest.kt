package com.rev.app.api.controller

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.UUID

class ThreadControllerTagWebMvcTest {

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    private fun mockMvcFor(controller: Any): MockMvc {
        val mapper = ObjectMapper().registerModule(JavaTimeModule())
        return MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(mapper))
            .build()
    }

    @Test
    fun list_with_tags_ok() {
        val controller = ThreadController(service) // ← 패키지 임포트 확인
        val mockMvc = mockMvcFor(controller)

        val boardId = UUID.randomUUID()
        val pageable: Pageable = PageRequest.of(0, 10)
        val tags = listOf("kotlin", "spring")

        val now = Instant.now()
        val sample = ThreadRes(
            id = UUID.randomUUID(),
            title = "title",
            content = "content",
            boardId = boardId,
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = null,
            createdAt = now,
            updatedAt = now,
            tags = tags
        )
        val page: Page<ThreadRes> = PageImpl(listOf(sample), pageable, 1)

        Mockito.`when`(
            service.listPublic(
                ArgumentMatchers.eq(boardId),
                ArgumentMatchers.any(Pageable::class.java),
                ArgumentMatchers.eq(tags)
            )
        ).thenReturn(page)

        mockMvc.perform(
            // 컨트롤러 매핑: GET /api/threads/{boardId}/threads
            MockMvcRequestBuilders.get("/api/threads/$boardId/threads")
                .param("page", "0")
                .param("size", "10")
                .param("tags", "kotlin")
                .param("tags", "spring")
                .accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }
}

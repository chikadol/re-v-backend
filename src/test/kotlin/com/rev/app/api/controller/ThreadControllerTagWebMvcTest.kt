package com.rev.app.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.*

class ThreadControllerTagWebMvcTest {

    private val service: ThreadService = mock()

    private fun mockMvcFor(controller: Any): MockMvc {
        val mapper = ObjectMapper().registerModule(JavaTimeModule())
        val builder: org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder =
            MockMvcBuilders.standaloneSetup(controller)
        builder.setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
        builder.setMessageConverters(MappingJackson2HttpMessageConverter(mapper))
        return builder.build()
    }

    @Test
    fun list_with_tags_ok() {
        val controller = ThreadController(service) // ← 실제 패키지 경로 임포트 확인
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

        // ✅ boardId는 eq(boardId)로 고정, Pageable은 Mockito.any(Pageable::class.java), tags는 anyOrNull
        whenever(
            service.listPublic(
                eq(boardId),
                Mockito.any(Pageable::class.java),
                anyOrNull<List<String>>()        // 컨트롤러가 nullable이면 anyOrNull, non-null이면 eq(tags)로 바꾸세요.
            )
        ).thenReturn(page)

        // ✅ URL은 실제 컨트롤러 매핑과 일치해야 함:
        // @RequestMapping("/api/threads") + @GetMapping("/{boardId}/threads") 라고 가정
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/threads/$boardId/threads")
                .param("page", "0")
                .param("size", "10")
                .param("tags", "kotlin")
                .param("tags", "spring")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

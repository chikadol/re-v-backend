package com.rev.app.api.controller

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.*
import org.junit.jupiter.api.Disabled

@Disabled("임시 비활성화 - 테스트 환경/Mockito 정리 후 다시 살릴 예정")
class ThreadControllerTagWebMvcTest {

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    @Test
    fun list_with_tags_ok() {
        val boardId = UUID.randomUUID()
        val controller = ThreadController(service)

        val mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()

        val now = Instant.now()
        val res = ThreadRes(
            id = UUID.randomUUID(), title = "t", content = "c",
            boardId = boardId, parentThreadId = null, authorId = UUID.randomUUID(),
            isPrivate = false, categoryId = null, createdAt = now, updatedAt = now, tags = listOf("kotlin")
        )
        val page: Page<ThreadRes> = PageImpl(listOf(res), PageRequest.of(0, 10), 1)

        // ✅ 이 스텁들도 반드시 @Test 안에서
        Mockito.lenient().doReturn(page)
            .`when`(service).listPublic(
                ArgumentMatchers.eq(boardId),
                ArgumentMatchers.any(Pageable::class.java),
                ArgumentMatchers.eq(listOf("kotlin"))
            )

        Mockito.lenient().doReturn(page) // 보호 스텁
            .`when`(service).listPublic(
                ArgumentMatchers.eq(boardId),
                ArgumentMatchers.any(Pageable::class.java)
            )

        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("tags", "kotlin")
                .param("page", "0").param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }

}

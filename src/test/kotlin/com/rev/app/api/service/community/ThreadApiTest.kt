package com.rev.app.api.service.community

import com.rev.app.api.controller.ThreadController
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.*

class ThreadApiTest {

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    @Test
    fun listPublic_ok() {
        // given
        val boardId = UUID.randomUUID()
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
            tags = emptyList()
        )

        // PageableResolver가 만들어 줄 값과 동일하게 세팅
        val pageable: Pageable = PageRequest.of(0, 10)
        val page: Page<ThreadRes> = PageImpl(listOf(sample), pageable, 1)

        // ✅ 매처 없이, 정확히 이 인자 조합에 대해 스텁
        Mockito.`when`(
            service.listPublic(boardId, pageable)
        ).thenReturn(page)

        // 컨트롤러가 3인자 버전을 호출할 가능성도 있으니 방어적으로 하나 더
        Mockito.`when`(
            service.listPublic(boardId, pageable, null)
        ).thenReturn(page)

        // ✅ 여기서 PageableHandlerMethodArgumentResolver 추가
        val mockMvc: MockMvc = MockMvcBuilders
            .standaloneSetup(ThreadController(service))
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()

        // when & then
        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

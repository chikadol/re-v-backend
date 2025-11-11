package com.rev.app.api.controller

import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.test.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.*

class ThreadControllerTagWebMvcTest {

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    @Test
    fun list_with_tags_ok() {
        val boardId = UUID.randomUUID()
        val controller = ThreadController(service)
        val mockMvc = standaloneMvc(controller)

        val now = Instant.now()
        val res = ThreadRes(
            id = UUID.randomUUID(), title = "t", content = "c",
            boardId = boardId, parentThreadId = null, authorId = UUID.randomUUID(),
            isPrivate = false, categoryId = null, createdAt = now, updatedAt = now, tags = listOf("kotlin")
        )
        val page: Page<ThreadRes> = PageImpl(listOf(res), PageRequest.of(0, 10), 1)

        // 3-인자(태그 포함) 호출
        lenientReturn(page)
            .`when`(service).listPublic(
                eqK(boardId),
                anyK(Pageable::class.java),
                eqK(listOf("kotlin"))
            )
        // 2-인자(보호용)
        lenientReturn(page)
            .`when`(service).listPublic(
                eqK(boardId),
                anyK(Pageable::class.java)
            )

        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("tags", "kotlin")
                .param("page", "0").param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

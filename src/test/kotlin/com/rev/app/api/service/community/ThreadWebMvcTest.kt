package com.rev.app.api.service.community

import com.rev.app.api.controller.ThreadController
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

class ThreadWebMvcTest {

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    @Test
    fun listPublic_ok() {
        val boardId = UUID.randomUUID()

        val now = Instant.now()
        val sample = ThreadRes(
            id = UUID.randomUUID(), title = "t", content = "c",
            boardId = boardId, parentThreadId = null, authorId = UUID.randomUUID(),
            isPrivate = false, categoryId = null, createdAt = now, updatedAt = now, tags = emptyList()
        )
        val page: Page<ThreadRes> = PageImpl(listOf(sample), PageRequest.of(0, 10), 1)

        lenientReturn(page)
            .`when`(service).listPublic(eqK(boardId), anyK(Pageable::class.java))
        lenientReturn(page)
            .`when`(service).listPublic(eqK(boardId), anyK(Pageable::class.java), anyListK<String>())

        val mockMvc = standaloneMvc(ThreadController(service))
        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0").param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

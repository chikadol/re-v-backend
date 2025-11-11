package com.rev.app.api.service.community

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rev.app.api.controller.ThreadController
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class ThreadWebMvcTest {

    private val service: ThreadService = Mockito.mock(ThreadService::class.java)

    private fun mvc(): MockMvc {
        val om = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        return MockMvcBuilders.standaloneSetup(ThreadController(service))
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(om))
            .build()
    }

    @Test
    fun listPublic_ok() {
        val boardId = UUID.randomUUID()
        val page: Page<ThreadRes> = PageImpl(emptyList(), PageRequest.of(0, 10), 0)

        Mockito.doReturn(page).`when`(service).listPublic(
            ArgumentMatchers.eq(boardId),
            ArgumentMatchers.any(Pageable::class.java)
        )

        mvc().perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0").param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk)
    }
}

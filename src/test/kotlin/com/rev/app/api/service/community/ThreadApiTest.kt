package com.rev.app.api.service.community

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rev.app.api.common.ApiExceptionHandler
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.core.MethodParameter
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID
import java.util.UUID.randomUUID

class ThreadApiTest {

    private lateinit var mockMvc: MockMvc
    private val threadService: ThreadService = mock()
    private lateinit var objectMapper: ObjectMapper

    // 테스트용 @AuthenticationPrincipal 리졸버
    private class FakeAuthPrincipalResolver(
        private val fixedUserId: UUID
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(param: MethodParameter): Boolean {
            val hasAnno = param.hasParameterAnnotation(AuthenticationPrincipal::class.java)
            val isType = JwtPrincipal::class.java.isAssignableFrom(param.parameterType)
            return hasAnno && isType
        }
        override fun resolveArgument(
            param: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any? {
            return JwtPrincipal(
                userId = fixedUserId,
                email = "",
                roles = listOf("test")
            )
        }
    }

    @BeforeEach
    fun setup() {
        val controller = ThreadControllerImpl(threadService)

        objectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val pageableResolver = PageableHandlerMethodArgumentResolver()
        val authResolver = FakeAuthPrincipalResolver(
            UUID.fromString("11111111-1111-1111-1111-111111111111")
        )

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiExceptionHandler())
            .setCustomArgumentResolvers(pageableResolver, authResolver)
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    @Test
    fun listPublic_ok() {
        val dto = ThreadRes(
            id = 1L, title = "hello", content = "world",
            authorId = randomUUID(), tags = listOf("tag1","tag2"),
            categoryId = randomUUID(), parentThreadId = null,
            isPrivate = false, createdAt = null, updatedAt = null
        )

        whenever(threadService.listPublic(any(), any())).thenAnswer {
            val pageable = it.getArgument<Pageable>(1)
            PageImpl(listOf(dto), pageable.takeIf { p -> p.isPaged } ?: PageRequest.of(0,10), 1)
        }

        mockMvc.perform(
            get("/api/threads/{boardId}/threads", 1L)
                .param("page","0")
                .param("size","10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
    }

    @Test
    fun listPublic_pagingAndSort_ok() {
        val dto = ThreadRes(
            id = 2L, title = "title", content = "c",
            authorId = randomUUID(), tags = listOf("a"),
            categoryId = randomUUID(), parentThreadId = null,
            isPrivate = false, createdAt = null, updatedAt = null
        )

        whenever(threadService.listPublic(any(), any())).thenAnswer {
            val pageable = it.getArgument<Pageable>(1)
            assertThat(pageable.pageNumber).isEqualTo(1)
            assertThat(pageable.pageSize).isEqualTo(5)
            assertThat(pageable.sort.getOrderFor("createdAt")!!.isDescending).isTrue
            PageImpl(listOf(dto), pageable, 12)
        }

        mockMvc.perform(
            get("/api/threads/{boardId}/threads", 99L)
                .param("page","1")
                .param("size","5")
                .param("sort","createdAt,desc")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(2))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.totalElements").value(12))

        verify(threadService).listPublic(eq(99L), any())
    }

    @Test
    fun listPublic_invalidSortKey_400() {
        mockMvc.perform(
            get("/api/threads/{boardId}/threads", 1L)
                .param("page","0")
                .param("size","10")
                .param("sort","hackerField,asc")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun createInBoard_ok() {
        val req = CreateThreadReq(title = "t", content = "c", tags = listOf("x"))
        val res = ThreadRes(
            id = 10L, title = "t", content = "c",
            authorId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            tags = listOf("x"), categoryId = randomUUID(),
            parentThreadId = null, isPrivate = false, createdAt = null, updatedAt = null
        )
        whenever(threadService.createInBoard(any(), eq(7L), any())).thenReturn(res)

        mockMvc.perform(
            post("/api/threads/boards/{boardId}", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.title").value("t"))

        verify(threadService).createInBoard(
            eq(UUID.fromString("11111111-1111-1111-1111-111111111111")),
            eq(7L),
            any()
        )
    }

    @Test
    fun createInBoard_badRequest_400() {
        val invalidReq = mapOf("title" to " ", "content" to "")
        mockMvc.perform(
            post("/api/threads/boards/{boardId}", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }
}

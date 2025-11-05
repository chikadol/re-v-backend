package com.rev.app.api.service.community

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rev.app.api.common.ApiExceptionHandler
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.core.MethodParameter
import org.springframework.data.domain.Page
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
        ): Any {
            return JwtPrincipal(
                userId = fixedUserId,
                email = "test@example.com",
                roles = listOf("test")
            )
        }
    }

    @BeforeEach
    fun setup() {
        val controller = ThreadController(threadService)

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

    private fun sampleRes(boardId: UUID, id: UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")) =

        ThreadRes(
            id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            title = "hello",
            content = "world",
            boardId = boardId,                 // ✅ 채움
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = UUID.randomUUID(),
            createdAt = null,
            updatedAt = null,
            tags = listOf("tag1", "tag2")
        )

    @Test
    fun listPublic_ok() {
        val boardId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val dto = sampleRes(boardId)

        whenever(threadService.listPublic(eq(boardId), any<Pageable>())).thenAnswer {
            val pageable = it.getArgument<Pageable>(1)
            PageImpl(listOf(dto), pageable, 1)
        }

        mockMvc.perform(
            get("/api/threads/{boardId}/threads", boardId.toString())
                .param("page","0")
                .param("size","10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(dto.id.toString()))
            .andExpect(jsonPath("$.content[0].boardId").value(boardId.toString()))
    }

    @Test
    fun listPublic_invalidSortKey_400() {
        val boardId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")

        // 서비스는 호출조차 되지 않아야 하므로 stubbing 불필요
        mockMvc.perform(
            get("/api/threads/{boardId}/threads", boardId.toString())
                .param("page","0")
                .param("size","10")
                .param("sort","hackerField,asc") // 허용되지 않은 정렬키
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun createInBoard_ok() {
        val principalUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val boardId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val req = CreateThreadReq(title = "t", content = "c", tags = listOf("x"))

        val res = ThreadRes(
            id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            title = "t",
            content = "c",
            boardId = boardId,
            parentThreadId = null,
            authorId = null,
            isPrivate = false,
            categoryId = null,
            createdAt = null,
            updatedAt = null,
            tags = listOf("x")
        )

        whenever(threadService.createInBoard(eq(principalUserId), eq(boardId), any())).thenReturn(res)

        mockMvc.perform(
            post("/api/threads/boards/{boardId}", boardId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(res.id.toString()))
            .andExpect(jsonPath("$.title").value("t"))
            .andExpect(jsonPath("$.boardId").value(boardId.toString()))

        verify(threadService).createInBoard(eq(principalUserId), eq(boardId), any())
    }
}

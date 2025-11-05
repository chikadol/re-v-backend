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
                email = "t@example.com",
                roles = listOf("test")
            )
        }
    }

    private val fixedUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val fixedBoardId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")

    @BeforeEach
    fun setup() {
        val controller = ThreadController(threadService)

        objectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val pageableResolver = PageableHandlerMethodArgumentResolver()
        val authResolver = FakeAuthPrincipalResolver(fixedUserId)

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiExceptionHandler())
            .setCustomArgumentResolvers(pageableResolver, authResolver)
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    @Test
    fun listPublic_ok() {
        val dto = ThreadRes(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            title = "hello",
            content = "world",
            boardId = fixedBoardId,
            parentThreadId = null,
            authorId = fixedUserId,
            isPrivate = false,
            categoryId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            createdAt = null,
            updatedAt = null,
            tags = listOf("tag1","tag2")
        )

        whenever(threadService.listPublic(eq(fixedBoardId)))
            .thenReturn(listOf(dto))

        mockMvc.perform(
            get("/api/threads/{boardId}/threads", fixedBoardId.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(dto.id.toString()))
            .andExpect(jsonPath("$[0].boardId").value(fixedBoardId.toString()))
            .andExpect(jsonPath("$[0].title").value("hello"))
    }

    @Test
    fun listPublic_invalidSortKey_400() {
        // 컨트롤러가 sort 파라미터를 검증한다면 유지, 아니라면 이 테스트는 제거/수정
        mockMvc.perform(
            get("/api/threads/{boardId}/threads", fixedBoardId.toString())
                .param("sort","hackerField,asc")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun createInBoard_ok() {
        val req = CreateThreadReq(title = "t", content = "c", tags = listOf("x"))
        val res = ThreadRes(
            id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
            title = "t",
            content = "c",
            boardId = fixedBoardId,
            parentThreadId = null,
            authorId = fixedUserId,
            isPrivate = false,
            categoryId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            createdAt = null, updatedAt = null,
            tags = listOf("x")
        )
        whenever(threadService.createInBoard(eq(fixedUserId), eq(fixedBoardId), any()))
            .thenReturn(res)

        mockMvc.perform(
            post("/api/threads/boards/{boardId}", fixedBoardId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(res.id.toString()))
            .andExpect(jsonPath("$.title").value("t"))

        verify(threadService).createInBoard(eq(fixedUserId), eq(fixedBoardId), any())
    }
}

package com.rev.app.api.service.community

import com.rev.app.api.common.ApiExceptionHandler
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
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
    private val service: ThreadService = mock()

    /**
     * @AuthenticationPrincipal 없이도 JwtPrincipal 주입을 흉내내는 테스트용 리졸버
     * 컨트롤러 메서드 파라미터 타입이 JwtPrincipal이면 이 리졸버가 값을 넣어줍니다.
     */
    private class FakeAuthResolver(
        private val uid: UUID
    ) : HandlerMethodArgumentResolver {

        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return JwtPrincipal::class.java.isAssignableFrom(parameter.parameterType)
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any {
            return JwtPrincipal(
                userId = uid,
                email = "t@t",
                roles = listOf("test")
            )
        }
    }

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(ThreadController(service))
            .setControllerAdvice(ApiExceptionHandler())
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                FakeAuthResolver(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            )
            .setMessageConverters(MappingJackson2HttpMessageConverter())
            .build()
    }

    @Test
    fun listPublic_ok() {
        val dto = ThreadRes(
            id = UUID.randomUUID(),
            title = "t",
            content = "c",
            boardId = UUID.randomUUID(),
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = null,
            createdAt = null,
            updatedAt = null,
            tags = listOf("x")
        )

        whenever(service.listPublic(any<UUID>(), any<Pageable>())).thenAnswer { invocation ->
            val pageable = invocation.getArgument<Pageable>(1)
            assertThat(pageable.pageSize).isEqualTo(10)
            PageImpl(listOf(dto), pageable, 1L)
        }

        mockMvc.perform(
            get("/api/threads/{boardId}/threads", UUID.randomUUID())
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].title").value("t"))
    }

    @Test
    fun createInBoard_ok() {
        val req = CreateThreadReq(
            title = "t",
            content = "c",
            tags = listOf("x")
        )

        val res = ThreadRes(
            id = UUID.randomUUID(),
            title = "t",
            content = "c",
            boardId = UUID.randomUUID(),
            parentThreadId = null,
            authorId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            isPrivate = false,
            categoryId = null,
            createdAt = null,
            updatedAt = null,
            tags = listOf("x")
        )

        // service.createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes
        whenever(service.createInBoard(any<UUID>(), any<UUID>(), eq(req))).thenReturn(res)

        mockMvc.perform(
            post("/api/threads/boards/{boardId}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"t","content":"c","tags":["x"]}""")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("t"))
    }
}

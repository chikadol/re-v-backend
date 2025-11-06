package com.rev.app.api.service.community

import com.rev.app.api.security.MeArgumentResolver
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.JwtAuthenticationFilter
import com.rev.app.auth.jwt.JwtProvider
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID
@WebMvcTest(controllers = [ThreadController::class])
@AutoConfigureMockMvc(addFilters = false)
class ThreadWebMvcTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var threadService: ThreadService

    // 컨트롤러에 끼어드는 글로벌 빈들 모킹
    @MockBean lateinit var meArgumentResolver: MeArgumentResolver
    @MockBean lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @Test
    fun listPublic_ok() {
        val boardUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val dto = ThreadRes(
            id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            title = "hello",
            content = "world",
            boardId = boardUuid,                 // ✅ 채움
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = UUID.randomUUID(),
            createdAt = null,
            updatedAt = null,
            tags = listOf("tag1", "tag2")
        )
        whenever(threadService.listPublic(eq(boardUuid), any())).thenReturn(
            PageImpl(listOf(dto), PageRequest.of(0, 10), 1)
        )

        mockMvc.get("/api/threads/{boardId}/threads", boardUuid.toString()) {
            param("page","0"); param("size","10")
            param("sort","hackerField,asc") // 허용 안됨
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isBadRequest() }       // ✅ 400
        }

        @Test
        fun listPublic_invalidSort_400() {
            val boardId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")

            mockMvc.get("/api/threads/{boardId}/threads", boardId.toString()) {
                param("page", "0"); param("size", "10")
                param("sort", "hackerField,asc")
                accept(MediaType.APPLICATION_JSON)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }
}

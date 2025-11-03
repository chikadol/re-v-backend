package com.rev.app.api.service.community

import com.rev.app.api.security.MeArgumentResolver
import com.rev.app.auth.JwtAuthenticationFilter
import com.rev.app.auth.jwt.JwtProvider
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID.randomUUID
import com.rev.app.api.service.community.ThreadController

@WebMvcTest(controllers = [ThreadController::class])
@AutoConfigureMockMvc(addFilters = false) // 🔑 보안필터 비활성화
class ThreadWebMvcTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockBean lateinit var threadService: ThreadService

    // 🔑 컨트롤러 처리에 끼어드는 글로벌 컴포넌트들은 목 처리
    @MockBean lateinit var meArgumentResolver: MeArgumentResolver
    @MockBean lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @Test
    fun listPublic_ok() {
        val dto = ThreadRes(
            id = 1L, title = "hello", content = "world",
            authorId = randomUUID(), tags = listOf("tag1","tag2"),
            categoryId = randomUUID(), parentThreadId = null,
            isPrivate = false, createdAt = null, updatedAt = null
        )
        whenever(threadService.listPublic(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenAnswer { PageImpl(listOf(dto), PageRequest.of(0,10), 1) }

        mockMvc.get("/api/threads/{boardId}/threads", 1L) {
            param("page","0"); param("size","10")
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].id") { value(1) }
        }
    }

    @Test
    fun listPublic_invalidSort_400() {
        mockMvc.get("/api/threads/{boardId}/threads", 1L) {
            param("page","0"); param("size","10")
            param("sort","hackerField,asc")
        }.andExpect {
            status { isBadRequest() }
        }
    }
}

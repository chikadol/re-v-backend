package com.rev.app.api.service.community

import com.rev.app.api.security.MeArgumentResolver
import com.rev.app.auth.JwtAuthenticationFilter
import com.rev.app.auth.jwt.JwtProvider
import com.rev.app.api.service.community.dto.ThreadRes
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(controllers = [ThreadController::class])
@AutoConfigureMockMvc(addFilters = false)
class ThreadWebMvcTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockBean lateinit var threadService: ThreadService

    @MockBean lateinit var meArgumentResolver: MeArgumentResolver
    @MockBean lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @Test
    fun listPublic_ok() {
        val boardId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val authorId = UUID.fromString("11111111-1111-1111-1111-111111111111")

        val dto = ThreadRes(
            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            title = "hello",
            content = "world",
            boardId = boardId,
            parentThreadId = null,
            authorId = authorId,
            isPrivate = false,
            categoryId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            createdAt = null, updatedAt = null,
            tags = listOf("tag1","tag2")
        )
        whenever(threadService.listPublic(boardId)).thenReturn(listOf(dto))

        mockMvc.get("/api/threads/{boardId}/threads", boardId.toString()) {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value(dto.id.toString()) }
            jsonPath("$[0].boardId") { value(boardId.toString()) }
        }
    }

    @Test
    fun listPublic_invalidSort_400() {
        val boardId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        mockMvc.get("/api/threads/{boardId}/threads", boardId.toString()) {
            param("sort","hackerField,asc")
        }.andExpect {
            status { isBadRequest() }
        }
    }
}

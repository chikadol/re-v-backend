package com.rev.app.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.rev.app.api.controller.dto.ThreadCreateRequest
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadDetailRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.security.JwtPrincipal
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.*

@WebMvcTest(ThreadController::class)
class ThreadControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var threadService: ThreadService

    @Test
    fun `게시글 목록 조회 - 성공`() {
        // Given
        val boardId = UUID.randomUUID()
        val threadRes = ThreadRes(
            id = UUID.randomUUID(),
            title = "테스트 게시글",
            content = "내용",
            boardId = boardId,
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            tags = emptyList()
        )
        val page = PageImpl(listOf(threadRes), PageRequest.of(0, 20), 1)

        whenever(threadService.listPublic(eq(boardId), any())).thenReturn(page)

        // When & Then
        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].title").value("테스트 게시글"))
    }

    @Test
    fun `게시글 목록 조회 - 태그 필터`() {
        // Given
        val boardId = UUID.randomUUID()
        val threadRes = ThreadRes(
            id = UUID.randomUUID(),
            title = "태그 게시글",
            content = "내용",
            boardId = boardId,
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            tags = listOf("kotlin")
        )
        val page = PageImpl(listOf(threadRes), PageRequest.of(0, 20), 1)

        whenever(threadService.listPublic(eq(boardId), any(), eq(listOf("kotlin")))).thenReturn(page)

        // When & Then
        mockMvc.perform(
            get("/api/threads/$boardId/threads")
                .param("tags", "kotlin")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].tags[0]").value("kotlin"))
    }

    @Test
    fun `게시글 상세 조회 - 성공`() {
        // Given
        val threadId = UUID.randomUUID()
        val boardId = UUID.randomUUID()
        val threadRes = ThreadRes(
            id = threadId,
            title = "상세 게시글",
            content = "내용",
            boardId = boardId,
            parentThreadId = null,
            authorId = UUID.randomUUID(),
            isPrivate = false,
            categoryId = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            tags = emptyList()
        )
        val detailRes = ThreadDetailRes(
            thread = threadRes,
            commentCount = 5L,
            bookmarkCount = 3L,
            reactions = mapOf("LIKE" to 10L, "LOVE" to 2L),
            myReaction = null,
            bookmarked = false
        )

        whenever(threadService.getDetail(eq(threadId), any())).thenReturn(detailRes)

        // When & Then
        mockMvc.perform(
            get("/api/threads/$threadId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.thread.title").value("상세 게시글"))
            .andExpect(jsonPath("$.data.commentCount").value(5))
            .andExpect(jsonPath("$.data.bookmarkCount").value(3))
    }

    @Test
    fun `게시글 상세 조회 - 존재하지 않는 게시글`() {
        // Given
        val threadId = UUID.randomUUID()

        whenever(threadService.getDetail(eq(threadId), any()))
            .thenThrow(IllegalArgumentException("Thread not found: $threadId"))

        // When & Then
        mockMvc.perform(
            get("/api/threads/$threadId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
    }

    @Test
    fun `게시글 생성 - 인증 필요`() {
        // Given
        val boardId = UUID.randomUUID()
        val request = ThreadCreateRequest(
            title = "새 게시글",
            content = "내용",
            isPrivate = false
        )

        // When & Then - 인증 없이 요청 시 401 반환
        mockMvc.perform(
            post("/api/threads/$boardId/threads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized) // 인증이 필요하므로 401 예상
    }
}


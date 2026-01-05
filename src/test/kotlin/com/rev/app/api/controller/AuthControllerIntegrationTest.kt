package com.rev.app.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.rev.app.auth.AuthService
import com.rev.app.auth.dto.LoginRequest
import com.rev.app.auth.dto.SignUpRequest
import com.rev.app.auth.dto.TokenResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(com.rev.app.auth.AuthController::class)
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `회원가입 - 성공`() {
        // Given
        val request = SignUpRequest(
            email = "test@example.com",
            username = "testuser",
            password = "password123",
            role = "USER"
        )
        val tokenResponse = TokenResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token"
        )

        whenever(authService.register(any(), any(), any(), any())).thenReturn(tokenResponse)

        // When & Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
    }

    @Test
    fun `회원가입 - 유효성 검사 실패`() {
        // Given
        val request = SignUpRequest(
            email = "invalid-email", // 잘못된 이메일 형식
            username = "",
            password = "123", // 너무 짧은 비밀번호
            role = "USER"
        )

        // When & Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `로그인 - 성공`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )
        val tokenResponse = TokenResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token"
        )

        whenever(authService.loginByEmail(any(), any())).thenReturn(tokenResponse)

        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
    }

    @Test
    fun `로그인 - 잘못된 자격증명`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrong-password"
        )

        whenever(authService.loginByEmail(any(), any()))
            .thenThrow(IllegalArgumentException("Invalid credentials"))

        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `토큰 갱신 - 성공`() {
        // Given
        val refreshToken = "valid-refresh-token"
        val tokenResponse = TokenResponse(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token"
        )

        whenever(authService.refresh(any())).thenReturn(tokenResponse)

        // When & Then
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$refreshToken"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
    }

    @Test
    fun `토큰 갱신 - 유효하지 않은 토큰`() {
        // Given
        val refreshToken = "invalid-token"

        whenever(authService.refresh(any()))
            .thenThrow(IllegalArgumentException("Invalid refresh token"))

        // When & Then
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$refreshToken"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }
}


package com.rev.app.auth

import com.rev.app.auth.jwt.JwtProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var jwtProvider: JwtProvider
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        jwtProvider = mock()
        passwordEncoder = mock()
        authService = AuthService(userRepository, jwtProvider, passwordEncoder)
    }

    @Test
    fun `loginByEmail - 성공`() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = UUID.randomUUID()
        val user = UserEntity(
            id = userId,
            email = email,
            username = "testuser",
            password = "encodedPassword",
            role = UserRole.USER
        )

        whenever(userRepository.findByEmail(email)).thenReturn(user)
        whenever(passwordEncoder.matches(password, user.password)).thenReturn(true)
        whenever(jwtProvider.generateAccessToken(userId, listOf("USER"))).thenReturn("accessToken")
        whenever(jwtProvider.generateRefreshToken(userId)).thenReturn("refreshToken")

        // When
        val result = authService.loginByEmail(email, password)

        // Then
        assertNotNull(result)
        assertEquals("accessToken", result?.accessToken)
        assertEquals("refreshToken", result?.refreshToken)
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder).matches(password, user.password)
    }

    @Test
    fun `loginByEmail - 이메일 없음`() {
        // Given
        val email = "notfound@example.com"
        val password = "password123"

        whenever(userRepository.findByEmail(email)).thenReturn(null)

        // When & Then
        assertThrows<IllegalArgumentException> {
            authService.loginByEmail(email, password)
        }
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder, never()).matches(any(), any())
    }

    @Test
    fun `loginByEmail - 비밀번호 불일치`() {
        // Given
        val email = "test@example.com"
        val password = "wrongPassword"
        val userId = UUID.randomUUID()
        val user = UserEntity(
            id = userId,
            email = email,
            username = "testuser",
            password = "encodedPassword",
            role = UserRole.USER
        )

        whenever(userRepository.findByEmail(email)).thenReturn(user)
        whenever(passwordEncoder.matches(password, user.password)).thenReturn(false)

        // When & Then
        assertThrows<IllegalArgumentException> {
            authService.loginByEmail(email, password)
        }
        verify(userRepository).findByEmail(email)
        verify(passwordEncoder).matches(password, user.password)
    }

    @Test
    fun `register - 성공`() {
        // Given
        val email = "newuser@example.com"
        val username = "newuser"
        val password = "password123"
        val userId = UUID.randomUUID()
        val encodedPassword = "encodedPassword"

        whenever(userRepository.findByEmail(email)).thenReturn(null)
        whenever(userRepository.findByUsername(username)).thenReturn(null)
        whenever(passwordEncoder.encode(password)).thenReturn(encodedPassword)
        whenever(jwtProvider.generateAccessToken(any(), any())).thenReturn("accessToken")
        whenever(jwtProvider.generateRefreshToken(any())).thenReturn("refreshToken")

        val savedUser = UserEntity(
            id = userId,
            email = email,
            username = username,
            password = encodedPassword,
            role = UserRole.USER
        )

        whenever(userRepository.saveAndFlush(any())).thenReturn(savedUser)

        // When
        val result = authService.register(email, username, password, "USER")

        // Then
        assertNotNull(result)
        assertEquals("accessToken", result.accessToken)
        assertEquals("refreshToken", result.refreshToken)
        verify(userRepository).findByEmail(email)
        verify(userRepository).findByUsername(username)
        verify(passwordEncoder).encode(password)
        verify(userRepository).saveAndFlush(any())
    }

    @Test
    fun `register - 이메일 중복`() {
        // Given
        val email = "existing@example.com"
        val username = "newuser"
        val password = "password123"
        val existingUser = UserEntity(
            id = UUID.randomUUID(),
            email = email,
            username = "existing",
            password = "encoded",
            role = UserRole.USER
        )

        whenever(userRepository.findByEmail(email)).thenReturn(existingUser)

        // When & Then
        assertThrows<IllegalArgumentException> {
            authService.register(email, username, password, "USER")
        }
        verify(userRepository).findByEmail(email)
        verify(userRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `register - 사용자명 중복`() {
        // Given
        val email = "new@example.com"
        val username = "existinguser"
        val password = "password123"
        val existingUser = UserEntity(
            id = UUID.randomUUID(),
            email = "other@example.com",
            username = username,
            password = "encoded",
            role = UserRole.USER
        )

        whenever(userRepository.findByEmail(email)).thenReturn(null)
        whenever(userRepository.findByUsername(username)).thenReturn(existingUser)

        // When & Then
        assertThrows<IllegalArgumentException> {
            authService.register(email, username, password, "USER")
        }
        verify(userRepository).findByEmail(email)
        verify(userRepository).findByUsername(username)
        verify(userRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `refresh - 성공`() {
        // Given
        val refreshToken = "validRefreshToken"
        val userId = UUID.randomUUID()
        val user = UserEntity(
            id = userId,
            email = "test@example.com",
            username = "testuser",
            password = "encoded",
            role = UserRole.USER
        )

        whenever(jwtProvider.validate(refreshToken)).thenReturn(true)
        whenever(jwtProvider.getUserId(refreshToken)).thenReturn(userId)
        whenever(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user))
        whenever(jwtProvider.generateAccessToken(userId, listOf("USER"))).thenReturn("newAccessToken")
        whenever(jwtProvider.generateRefreshToken(userId)).thenReturn("newRefreshToken")

        // When
        val result = authService.refresh(refreshToken)

        // Then
        assertNotNull(result)
        assertEquals("newAccessToken", result.accessToken)
        assertEquals("newRefreshToken", result.refreshToken)
        verify(jwtProvider).validate(refreshToken)
        verify(jwtProvider).getUserId(refreshToken)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `refresh - 유효하지 않은 토큰`() {
        // Given
        val refreshToken = "invalidToken"

        whenever(jwtProvider.validate(refreshToken)).thenReturn(false)

        // When & Then
        assertThrows<IllegalArgumentException> {
            authService.refresh(refreshToken)
        }
        verify(jwtProvider).validate(refreshToken)
        verify(jwtProvider, never()).getUserId(any())
    }
}


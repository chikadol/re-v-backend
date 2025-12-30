package com.rev.app.auth

import com.rev.app.auth.dto.TokenResponse
import com.rev.app.auth.jwt.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

    @Service
    class AuthService(
        private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder
    ) {
    fun loginByEmail(email: String, password: String): TokenResponse? {
            val user = userRepository.findByEmail(email)
                ?: throw IllegalArgumentException("Invalid credentials")

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        return user.id?.let { userId ->
            TokenResponse(
                accessToken = jwtProvider.generateAccessToken(userId),
                refreshToken = jwtProvider.generateRefreshToken(userId)
            )
        }
        }

    fun login(userId: UUID): TokenResponse {
            val access = jwtProvider.generateAccessToken(userId)
            val refresh = jwtProvider.generateRefreshToken(userId)

        return TokenResponse(
            accessToken = access,
            refreshToken = refresh
        )
        }

        fun refresh(refreshToken: String): TokenResponse {
            if (!jwtProvider.validate(refreshToken)) {
                throw IllegalArgumentException("Invalid refresh token")
            }

            val userId = jwtProvider.getUserId(refreshToken)

        return TokenResponse(
            accessToken = jwtProvider.generateAccessToken(userId),
            refreshToken = jwtProvider.generateRefreshToken(userId)
            )
        }

    @Transactional
    fun register(email: String, username: String, password: String, role: String): TokenResponse {
        // 이메일 중복 확인
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }
        
        // 사용자명 중복 확인
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("이미 사용 중인 사용자명입니다.")
        }
        
        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(password)
        
        // 새 사용자 생성
        val newUser = UserEntity(
            email = email,
            username = username,
            password = encodedPassword,
            role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrElse { UserRole.USER }
        )
        
        val savedUser = userRepository.saveAndFlush(newUser)
        
        // 회원가입 후 자동 로그인
        return savedUser.id?.let { userId ->
            TokenResponse(
                accessToken = jwtProvider.generateAccessToken(userId),
                refreshToken = jwtProvider.generateRefreshToken(userId)
            )
        } ?: throw IllegalStateException("사용자 생성 실패")
    }

        private inline fun <reified T> tryGet(target: Any, methodName: String): T? =
            try { target::class.java.getMethod(methodName).invoke(target) as? T }
            catch (_: Exception) { null }
}

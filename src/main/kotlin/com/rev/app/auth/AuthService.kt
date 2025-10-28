package com.rev.app.auth

import com.rev.app.auth.dto.LoginRequest
import com.rev.app.auth.dto.SignUpRequest
import com.rev.app.auth.dto.TokenResponse
import com.rev.app.auth.jwt.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AuthService(
    private val users: UserRepository,
    private val pe: PasswordEncoder,
    private val jwt: JwtProvider,
) {
    @Transactional
    fun signUp(req: SignUpRequest): TokenResponse {
        require(!users.existsByEmail(req.email)) { "EMAIL_TAKEN" }
        val saved = users.save(UserEntity(email = req.email, password = pe.encode(req.password)))
        val access = jwt.generateAccessToken(saved.email, roles = listOf("USER"))
        val refresh = jwt.generateRefreshToken(saved.email)
        return TokenResponse(access, refresh)
    }


    fun login(req: LoginRequest): TokenResponse {
        val user = users.findByEmail(req.email) ?: error("INVALID_CREDENTIALS")
        check(pe.matches(req.password, user.password)) { "INVALID_CREDENTIALS" }
        val roles = user.roles.split(',').map { it.trim() }.filter { it.isNotBlank() }
        val access = jwt.generateAccessToken(user.email, roles)
        val refresh = jwt.generateRefreshToken(user.email)
        return TokenResponse(access, refresh)
    }


    fun refresh(refreshToken: String): TokenResponse {
        val subject = jwt.parseSubject(refreshToken) ?: error("INVALID_REFRESH")
        val roles = jwt.parseRoles(refreshToken).ifEmpty { listOf("USER") }
        val access = jwt.generateAccessToken(subject, roles)
        val newRefresh = jwt.generateRefreshToken(subject)
        return TokenResponse(access, newRefresh)
    }
}
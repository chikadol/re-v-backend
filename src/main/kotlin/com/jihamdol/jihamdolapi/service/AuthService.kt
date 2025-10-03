package com.jihamdol.jihamdolapi.service

import com.jihamdol.jihamdolapi.config.JwtTokenProvider
import com.jihamdol.jihamdolapi.domain.User
import com.jihamdol.jihamdolapi.domain.UserRepository
import com.jihamdol.jihamdolapi.dto.AuthRequest
import com.jihamdol.jihamdolapi.dto.AuthResponse
import com.jihamdol.jihamdolapi.dto.TokenResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {
    fun signup(request: AuthRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("username already exists")
        }
        val encoded = passwordEncoder.encode(request.password)
        val user = userRepository.save(User(username = request.username, password = encoded))
        val token = jwtTokenProvider.createToken(user.username)
        return AuthResponse(token)
    }

    fun login(request: AuthRequest): TokenResponse {
        val authToken = UsernamePasswordAuthenticationToken(request.username, request.password)
        authenticationManager.authenticate(authToken)

        val accessToken = jwtTokenProvider.createToken(request.username)
        val refreshToken = jwtTokenProvider.createRefreshToken(request.username)
        return TokenResponse(accessToken, refreshToken)
    }
}

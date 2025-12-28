package com.rev.app.auth

import com.rev.app.auth.dto.LoginRequest
import com.rev.app.auth.dto.TokenResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RefreshRequest(val refreshToken: String)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.loginByEmail(req.email, req.password)
            ?: throw IllegalArgumentException("Invalid credentials")
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.refresh(req.refreshToken)
        return ResponseEntity.ok(tokenResponse)
    }
}

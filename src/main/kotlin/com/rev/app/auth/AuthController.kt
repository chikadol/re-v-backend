package com.rev.app.auth

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class LoginRequest(val username: String, val roles: List<String> = listOf("ROLE_USER"))
data class TokenResponse(val accessToken: String, val refreshToken: String)
data class RefreshRequest(val refreshToken: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<TokenResponse> {
        val pair = authService.login(req.username, req.roles)
        return ResponseEntity.ok(TokenResponse(pair.accessToken, pair.refreshToken))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): ResponseEntity<TokenResponse> {
        val pair = authService.refresh(req.refreshToken)
        return ResponseEntity.ok(TokenResponse(pair.accessToken, pair.refreshToken))
    }
}

package com.rev.app.auth

import com.rev.app.auth.dto.TokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RefreshRequest(val refreshToken: String)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): ResponseEntity<TokenResponse?> {
        val pair = authService.refresh(req.refreshToken)
        return ResponseEntity.ok(pair)
    }
}

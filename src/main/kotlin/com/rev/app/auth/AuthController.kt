package com.rev.app.auth

import com.rev.app.auth.dto.LoginRequest
import com.rev.app.auth.dto.SignUpRequest
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
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: SignUpRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.register(req.email, req.username, req.password, req.role)
        return ResponseEntity.ok(tokenResponse)
    }

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

    /**
     * OAuth2 로그인 성공 후 백엔드의 /auth/callback으로 요청이 들어올 경우
     * 프론트엔드로 리다이렉트하는 백업 엔드포인트
     * 정상 플로우는 OAuth2SuccessHandler가 프론트엔드로 직접 리다이렉트합니다.
     */
    @GetMapping("/callback")
    fun oauth2Callback(
        @RequestParam(required = false) accessToken: String?,
        @RequestParam(required = false) refreshToken: String?,
        @RequestParam(required = false) provider: String?,
        response: jakarta.servlet.http.HttpServletResponse
    ) {
        // 토큰이 있으면 프론트엔드로 그대로 전달 (절대 URL 사용)
        val redirectUrl = if (accessToken != null && refreshToken != null) {
            org.springframework.web.util.UriComponentsBuilder
                .fromUriString("http://localhost:5173/auth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .apply { if (provider != null) queryParam("provider", provider) }
                .build()
                .toUriString()
        } else {
            // 토큰이 없으면 에러 페이지로
            "http://localhost:5173/login?error=oauth2_callback_missing_params"
        }

        response.sendRedirect(redirectUrl)
    }
}

package com.rev.app.auth

import com.rev.app.api.controller.ResponseHelper
import com.rev.app.api.controller.dto.ApiResponse
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
    fun register(@Valid @RequestBody req: SignUpRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        return try {
            val tokenResponse = authService.register(req.email, req.username, req.password, req.role)
            ResponseHelper.ok(tokenResponse, "회원가입이 완료되었습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.error("REGISTRATION_FAILED", e.message ?: "회원가입에 실패했습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("INTERNAL_ERROR", "회원가입 중 오류가 발생했습니다.")
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        return try {
            val tokenResponse = authService.loginByEmail(req.email, req.password)
                ?: throw IllegalArgumentException("Invalid credentials")
            ResponseHelper.ok(tokenResponse, "로그인에 성공했습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.error("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("LOGIN_FAILED", "로그인 중 오류가 발생했습니다.")
        }
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        return try {
            val tokenResponse = authService.refresh(req.refreshToken)
            ResponseHelper.ok(tokenResponse, "토큰이 갱신되었습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.error("INVALID_REFRESH_TOKEN", "유효하지 않은 토큰입니다.")
        } catch (e: Exception) {
            ResponseHelper.error("REFRESH_FAILED", "토큰 갱신 중 오류가 발생했습니다.")
        }
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

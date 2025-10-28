package com.rev.app.auth

import com.rev.app.auth.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/auth")
class AuthController(private val auth: AuthService) {


    @PostMapping("/signup")
    fun signUp(@RequestBody @Valid req: SignUpRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(auth.signUp(req))


    @PostMapping("/login")
    fun login(@RequestBody @Valid req: LoginRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(auth.login(req))


    @PostMapping("/refresh")
    fun refresh(@RequestHeader("Authorization") authz: String?): ResponseEntity<TokenResponse> {
        val token = authz?.removePrefix("Bearer ") ?: ""
        return ResponseEntity.ok(auth.refresh(token))
    }


    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: CustomUserDetails?): MeResponse =
        principal?.let { MeResponse(it.id(), it.username, it.authorities.map { a -> a.authority }) }
            ?: MeResponse(-1, "anonymous", emptyList())
}
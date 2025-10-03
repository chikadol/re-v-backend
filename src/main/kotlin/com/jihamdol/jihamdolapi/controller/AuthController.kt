package com.jihamdol.jihamdolapi.controller

import com.jihamdol.jihamdolapi.dto.AuthRequest
import com.jihamdol.jihamdolapi.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signup")
    fun signup(@Validated @RequestBody req: AuthRequest) =
        ResponseEntity.ok(authService.signup(req))

    @PostMapping("/login")
    fun login(@Validated @RequestBody req: AuthRequest) =
        ResponseEntity.ok(authService.login(req))
}

package com.rev.app.api

import com.rev.app.services.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val auth: AuthService) {

    @PostMapping("/signup")
    fun signup(@RequestBody req: SignupReq) = ResponseEntity.ok(mapOf("ok" to true).toMutableMap().apply {
        auth.signup(req.email, req.password)
    })

    @PostMapping("/login")
    fun login(@RequestBody req: LoginReq) = ResponseEntity.ok(auth.login(req.email, req.password))
}

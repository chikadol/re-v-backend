package com.rev.app.user

import org.springframework.web.bind.annotation.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

@RestController
class MeController {
    @GetMapping("/api/me")
    fun me(auth: Authentication): Map<String, Any?> {
        val roles = auth.authorities.map(GrantedAuthority::getAuthority) // 또는 { it.authority }
        return mapOf(
            "user" to auth.name,
            "roles" to roles
        )
    }
}

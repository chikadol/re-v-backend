package com.rev.app.auth

import com.rev.app.api.security.Me
import com.rev.app.api.security.MeDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class MeProfileDto(
    val userId: Long,
    val username: String,
    val roles: List<String>
)

@RestController
class MeController {
    @GetMapping("/api/me")
    fun me(@Me me: MeDto): ResponseEntity<MeProfileDto> =
        ResponseEntity.ok(
            MeProfileDto(
                userId = me.userId,
                username = me.username,
                roles = me.roles
            )
        )
}

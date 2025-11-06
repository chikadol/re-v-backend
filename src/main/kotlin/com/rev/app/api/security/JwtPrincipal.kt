package com.rev.app.api.security

import java.util.UUID

data class JwtPrincipal(
    val userId: UUID?,
    val email: String,
    val roles: List<String> = emptyList()
)

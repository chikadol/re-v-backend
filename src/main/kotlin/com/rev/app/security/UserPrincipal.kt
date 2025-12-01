package com.rev.app.security

import java.util.UUID

data class UserPrincipal(
    val id: UUID,
    val email: String
)

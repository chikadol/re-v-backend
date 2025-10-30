package com.rev.app.api.security

import java.util.UUID


data class MeDto(
    val userId: Long,
    val username: String,
    val roles: List<String>
)
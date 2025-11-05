package com.rev.app.api.service.community.dto

import java.util.UUID

data class BoardRes(
    val id: UUID?,
    val name: String,
    val slug: String,
    val description: String?
)

package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import java.util.UUID

data class BoardDto(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?
)

fun BoardRes.toDto(): BoardDto =
    BoardDto(
        id = requireNotNull(this.id),
        name = this.name,
        slug = this.slug,
        description = this.description
    )

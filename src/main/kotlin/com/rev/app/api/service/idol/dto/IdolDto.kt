package com.rev.app.api.service.idol.dto

import com.rev.app.domain.idol.IdolEntity
import java.util.UUID

data class IdolCreateRequest(
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null
)

data class IdolRes(
    val id: UUID,
    val name: String,
    val description: String?,
    val imageUrl: String?
) {
    companion object {
        fun from(entity: IdolEntity): IdolRes = IdolRes(
            id = requireNotNull(entity.id),
            name = entity.name,
            description = entity.description,
            imageUrl = entity.imageUrl
        )
    }
}


package com.rev.app.api.service.community

import com.rev.app.domain.community.entity.ThreadEntity
import java.util.UUID

data class ThreadBookmarkDto(
    val threadId: UUID
)

fun ThreadEntity.toBookmarkDto(): ThreadBookmarkDto =
    ThreadBookmarkDto(threadId = requireNotNull(id))

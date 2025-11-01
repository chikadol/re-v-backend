package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadBookmarkDto
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import java.time.Instant

fun ThreadBookmarkEntity.toRes(): ThreadBookmarkDto =
    ThreadBookmarkDto(
        id = requireNotNull(id),
        threadId = requireNotNull(thread.id),
        userId = requireNotNull(user.id),
        createdAt = this.createdAt ?: Instant.now()   // ← 수정
    )

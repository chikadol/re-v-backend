package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadDto
import com.rev.app.api.service.community.ReactionType
import java.time.Instant

// 도메인 엔티티 준비될 때 실제 매핑으로 교체.
// 지금은 컴파일용 안전 더미.
fun toThreadDtoDummy(): ThreadDto = ThreadDto(
    id = 1L,
    title = "dummy",
    content = "dummy",
    authorId = 1L,
    tags = emptyList(),
    categoryId = null,
    parentThreadId = null,
    isPrivate = false,
    createdAt = Instant.now(),
    updatedAt = null,
    myReaction = null,
    reactionCounts = mapOf(ReactionType.LIKE to 1L),
    bookmarked = false
)

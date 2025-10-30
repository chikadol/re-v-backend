// src/main/kotlin/com/rev/app/api/service/community/ThreadBookmarkMappers.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadBookmarkDto
import java.time.Instant

// ★ FQCN으로 receiver를 지정해서 import 충돌/그림자(이름 중복) 방지
fun com.rev.app.domain.community.entity.ThreadBookmarkEntity.toRes(): ThreadBookmarkDto {
    val id = requireNotNull(this.id) { "ThreadBookmarkEntity.id is null" }

    // thread/user가 지연 로딩일 수 있으므로 안전하게 id만 꺼냅니다.
    val threadId = requireNotNull(this.thread.id) { "ThreadBookmarkEntity.thread.id is null" }
    val userId   = requireNotNull(this.user.id)   { "ThreadBookmarkEntity.user.id is null" }

    // 만약 createdAt이 nullable 이라면 ?: Instant.now()로 방어하세요.
    val created = this.createdAt // 타입이 Instant(NonNull)라면 그대로 사용

    return ThreadBookmarkDto(
        id = id,
        threadId = threadId,
        userId = userId,
        createdAt = created
    )
}

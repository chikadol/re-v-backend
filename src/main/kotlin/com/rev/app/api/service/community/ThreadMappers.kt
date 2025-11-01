// src/main/kotlin/com/rev/app/api/service/community/ThreadMappers.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import java.time.Instant

// ── Entity -> DTO (스레드)
fun ThreadEntity.toThreadRes(): ThreadRes =
    ThreadRes(
        id = requireNotNull(id),
        title = title,
        content = content,
        authorId = requireNotNull(author.id),
        tags = tags,
        categoryId = categoryId,
        parentThreadId = parentId,
        isPrivate = isPrivate,
        createdAt = createdAt ?: Instant.now(),
        updatedAt = updatedAt
    )

// 이름이 겹치는 확장함수도 그대로 필요하면 얇게 위임
fun ThreadEntity.toRes(): ThreadRes = this.toThreadRes()

// ── Create 요청 -> Entity
fun CreateThreadReq.toEntity(author: UserEntity): ThreadEntity =
    ThreadEntity(
        title = this.title,
        content = this.content,
        author = author,
        tags = this.tags?.toMutableList() ?: mutableListOf(),
        categoryId = this.categoryId,
        parentId = this.parentThreadId,
        isPrivate = this.isPrivate ?: false
    )

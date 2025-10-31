// src/main/kotlin/com/rev/app/api/service/community/ThreadMappers.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import java.time.Instant

fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
        id = requireNotNull(id),
        title = title,
        content = content,
        authorId = requireNotNull(author.id),
        tags = tags,
        categoryId = categoryId,
        parentThreadId = parentThreadId,
        isPrivate = isPrivate,
        createdAt = createdAt ?: Instant.now(),  // ✅ BaseTime에서 가져옴
        updatedAt = updatedAt
    )

fun CreateThreadReq.toEntity(author: UserEntity): ThreadEntity =
    ThreadEntity(
        title = this.title,
        content = this.content,
        author = author,
        tags = this.tags?.toMutableList() ?: mutableListOf(),
        categoryId = this.categoryId,
        parentThreadId = this.parentThreadId,
        isPrivate = this.isPrivate ?: false
    )

// src/main/kotlin/com/rev/app/api/service/community/ThreadMappers.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.ThreadRes
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import java.time.Instant

fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
        id = requireNotNull(id),
        title = title,
        content = content,
        authorId = requireNotNull(author.id),   // ← 여기 수정 (UUID? -> UUID)
        tags = tags,
        categoryId = categoryId,
        parentThreadId = parentId,
        isPrivate = isPrivate,
        createdAt = createdAt ?: Instant.now(),
        updatedAt = updatedAt
    )

fun CreateThreadReq.toEntity(
    author: UserEntity,
    board: Board
): ThreadEntity =
    ThreadEntity(
        title = this.title,
        content = this.content,
        author = author,
        board = board,
        tags = this.tags?.toMutableList() ?: mutableListOf(),
        categoryId = this.categoryId,
        parentId = this.parentThreadId,
        isPrivate = this.isPrivate ?: false
    )

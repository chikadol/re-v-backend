// src/main/kotlin/com/rev/app/api/service/community/Mappers.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.entity.CommentEntity

fun Board.toRes() = BoardRes(id = id, name = name, slug = slug, description = description)

fun ThreadEntity.toRes() = ThreadRes(
    id = requireNotNull(id),
    title = title,
    content = content,
    boardId = board?.id,
    parentThreadId = parent?.id,
    authorId = author?.id,
    isPrivate = isPrivate,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = tags
)

fun CommentEntity.toRes() = CommentRes(
    id = requireNotNull(id),
    threadId = requireNotNull(thread?.id),
    authorId = author?.id,
    parentId = parent?.id,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

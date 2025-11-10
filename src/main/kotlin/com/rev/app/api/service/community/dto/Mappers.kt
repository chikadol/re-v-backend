package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import java.util.*

fun Board.toRes(): BoardRes = BoardRes(
    id = requireNotNull(id),
    name = name,
    slug = slug,
    description = description
)
fun ThreadEntity.toRes(): ThreadRes = ThreadRes(
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
    tags = emptyList()            // ✅ 기본값 유지
)

fun ThreadEntity.toResWithTags(tags: List<String>): ThreadRes =
    this.toRes().copy(tags = tags)

fun CommentEntity.toRes(): CommentRes = CommentRes(
    id = requireNotNull(id),
    threadId = requireNotNull(thread?.id),
    authorId = author?.id,
    parentId = parent?.id,
    content = content,
    createdAt = createdAt
)

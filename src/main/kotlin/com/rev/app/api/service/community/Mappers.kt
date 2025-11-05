package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardRes
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity

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
    tags = emptyList() // 태그 테이블 사용 시 연결
)

fun CommentEntity.toRes(): CommentRes = CommentRes(
    id = requireNotNull(id),
    threadId = thread?.id,
    authorId = author?.id,
    content = content,
    parentId = parent?.id,
    createdAt = createdAt,
    updatedAt = updatedAt
)

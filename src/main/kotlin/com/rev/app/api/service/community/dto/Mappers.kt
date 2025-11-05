package com.rev.app.api.service.community.dto

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity

/* ===== Board ===== */

fun Board.toRes(): BoardRes =
    BoardRes(
        id = requireNotNull(id),
        name = name,
        slug = slug,
        description = description
    )

/* ===== Thread ===== */

fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
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
        // 엔티티에 tags 필드가 없다면 빈 리스트 반환
        tags = emptyList()
    )

fun CreateThreadReq.toEntity(
    board: Board,
    author: UserEntity,
    parent: ThreadEntity? = null
): ThreadEntity =
    ThreadEntity(
        title = title,
        content = content,
        board = board,
        author = author,
        parent = parent,
        isPrivate = false,
        categoryId = null
        // 엔티티 생성자에 tags 파라미터가 **없으므로 전달하지 않음**
    )

/* ===== Comment ===== */

fun CommentEntity.toRes(): CommentRes =
    CommentRes(
        id = requireNotNull(id),
        threadId = requireNotNull(thread?.id),
        parentId = parent?.id,
        authorId = requireNotNull(author?.id),
        content = content,
        createdAt = createdAt
    )

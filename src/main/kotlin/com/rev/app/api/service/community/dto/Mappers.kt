package com.rev.app.api.service.community.dto

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import java.util.UUID

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
        id = this.id ?: UUID.randomUUID(),      // 또는 throw IllegalStateException("...") 로 교체
        title = this.title,
        content = this.content,
        boardId = this.board?.id,
        parentThreadId = this.parent?.id,
        authorId = this.author?.id,
        isPrivate = this.isPrivate,
        categoryId = this.categoryId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
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

package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.Board
import java.util.UUID

fun Board.toRes(): BoardRes =
    BoardRes(
        id = this.id ?: throw IllegalArgumentException("Board.id is null – persist before mapping"),
        name = this.name,
        slug = this.slug,
        description = this.description
    )

fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
        id = this.id ?: throw IllegalArgumentException("Thread.id is null – persist before mapping"),
        title = this.title,
        content = this.content,
        boardId = this.board?.id,                // 보드는 nullable 허용
        parentThreadId = this.parent?.id,        // 부모도 nullable
        authorId = this.author?.id,              // 작성자 nullable 허용
        isPrivate = this.isPrivate ?: false,     // nullable이면 기본 false
        categoryId = this.categoryId,            // nullable OK
        createdAt = this.createdAt,              // 엔티티에 있으면 그대로
        updatedAt = this.updatedAt,              // 엔티티에 있으면 그대로
        tags = this.tags ?: emptyList()
    )

fun CommentEntity.toRes(): CommentRes =
    CommentRes(
        id = this.id ?: throw IllegalArgumentException("Comment.id is null – persist before mapping"),
        threadId = this.thread?.id ?: throw IllegalArgumentException("Comment.thread.id is null"),
        authorId = this.author?.id,
        parentId = this.parent?.id,
        content = this.content,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

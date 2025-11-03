package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import java.time.Instant
import java.util.UUID

// 엔티티 -> 응답 DTO
fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
        id = requireNotNull(id),
        title = title,
        content = content,
        authorId = author.id as UUID,     // UserEntity.id = UUID
        tags = tags,
        categoryId = categoryId,
        parentThreadId = parentId,
        isPrivate = isPrivate,
        createdAt = this.createdAt ?: Instant.now(),
        updatedAt = this.updatedAt
    )

// 생성 요청 -> 엔티티  (boardId: Long ❌ → board: Board ✅)
fun CreateThreadReq.toEntity(board: Board, author: UserEntity): ThreadEntity =
    ThreadEntity(
        board = board,
        title = this.title,
        content = this.content,
        author = author,
        tags = this.tags.toMutableList(),
        categoryId = this.categoryId,
        parentId = this.parentThreadId,
        isPrivate = this.isPrivate
    )

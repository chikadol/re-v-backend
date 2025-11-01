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
        parentThreadId = parentId,
        isPrivate = isPrivate,
        createdAt = this.createdAt ?: Instant.now(),
        updatedAt = this.updatedAt
    )

fun CreateThreadReq.toEntity(author: UserEntity): ThreadEntity =
    ThreadEntity(
        title = this.title,
        content = this.content,
        author = author,
        tags = this.tags.toMutableList(),
        categoryId = this.categoryId,
        parentId = this.parentThreadId,
        isPrivate = this.isPrivate
    )

package com.rev.app.api.service.community


import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity

fun CreateThreadReq.toEntity(author: UserEntity): ThreadEntity =
    ThreadEntity(
        title = this.title,
        content = this.content,
        author = author,
        tags = this.tags?.toMutableList() ?: mutableListOf(),
        categoryId = this.categoryId,
        parentId = this.parentId,
        isPrivate = this.isPrivate ?: false
    )

fun ThreadEntity.toRes(): ThreadRes = ThreadRes.from(this)

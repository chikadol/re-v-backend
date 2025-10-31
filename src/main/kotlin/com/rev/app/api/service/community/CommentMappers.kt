package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.domain.community.entity.CommentEntity
import java.time.Instant

fun CommentEntity.toRes(): CommentRes =
    CommentRes(
        id = requireNotNull(id),
        threadId = requireNotNull(thread.id),
        authorId = requireNotNull(author.id),
        content = content,
        parentId = parentId,                        // ✅ 부모가 없으면 null
        createdAt = this.createdAt ?: Instant.now(),
        updatedAt = this.updatedAt
    )


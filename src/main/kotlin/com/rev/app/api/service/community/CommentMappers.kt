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
        parentId = this.parent?.id,   // ✅ parent는 엔티티, id만 노출
        createdAt = this.createdAt ?: Instant.now(),
        updatedAt = this.updatedAt
    )

// src/main/kotlin/com/rev/app/api/service/community/CommentMappers.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CommentDto
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.domain.community.entity.CommentEntity
import java.time.Instant
import java.util.UUID

// Entity -> DTO
fun CommentEntity.toDto(): CommentDto =
    CommentDto(
        id        = requireNotNull(this.id),
        threadId  = requireNotNull(this.thread.id),          // ManyToOne thread
        authorId  = this.author.id as UUID,                  // ManyToOne author -> UUID 꺼내기
        content   = this.content,
        parentId  = this.parent?.id,                         // parent Comment? -> id만 추출
        createdAt = this.createdAt ?: Instant.now(),         // nullable 방어
        updatedAt = this.updatedAt
    )

// DTO -> Response
fun CommentDto.toRes(): CommentRes =
    CommentRes(
        id        = id,
        threadId  = threadId,
        authorId  = authorId,
        content   = content,
        parentId  = parentId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

// 편의 매퍼: Entity -> Response
fun CommentEntity.toRes(): CommentRes = this.toDto().toRes()

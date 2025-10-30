// com.rev.app.api.service.community.ThreadRes.kt
package com.rev.app.api.service.community

import com.rev.app.domain.community.entity.ThreadEntity
import java.util.UUID

data class ThreadRes(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: UUID?,     // ← 여기서만 노출
    val tags: List<String>,
    val categoryId: UUID?,
    val parentId: UUID?,
    val isPrivate: Boolean
) {
    companion object {
        fun from(e: ThreadEntity) = ThreadRes(
            id = e.id!!,
            title = e.title,
            content = e.content,
            authorId = e.author.id,   // ← 엔티티에서 꺼냄
            tags = e.tags.toList(),
            categoryId = e.categoryId,
            parentId = e.parentId,
            isPrivate = e.isPrivate
        )
    }
}

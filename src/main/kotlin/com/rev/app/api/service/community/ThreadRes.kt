// com.rev.app.api.service.community.ThreadRes.kt
package com.rev.app.api.service.community

import com.rev.app.domain.community.entity.ThreadEntity
import java.util.UUID

data class ThreadRes(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: UUID?,
    val tags: List<String>,
    val categoryId: UUID?,
    val parentId: ThreadEntity?,
    val isPrivate: Boolean
) {
    companion object {
        fun from(e: ThreadEntity) = ThreadRes(
            id = e.id!!,
            title = e.title,
            content = e.content,
            authorId = e.author.id,
            tags = e.tags.toList(),
            categoryId = e.categoryId,
            parentId = e.parent,
            isPrivate = e.isPrivate
        )
    }
}
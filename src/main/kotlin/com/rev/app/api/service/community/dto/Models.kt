package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.entity.ThreadEntity
import java.time.Instant
import java.util.UUID

data class BoardRes(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?
)

data class BoardCreateRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "게시판 이름은 필수 항목입니다.")
    @field:jakarta.validation.constraints.Size(min = 1, max = 100, message = "게시판 이름은 1자 이상 100자 이하여야 합니다.")
    val name: String,
    
    @field:jakarta.validation.constraints.NotBlank(message = "슬러그는 필수 항목입니다.")
    @field:jakarta.validation.constraints.Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "슬러그는 소문자, 숫자, 하이픈(-)만 사용할 수 있습니다."
    )
    val slug: String,
    
    @field:jakarta.validation.constraints.Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    val description: String? = null
)

data class ThreadRes(
    val id: UUID,
    val title: String,
    val content: String,
    val boardId: UUID?,
    val parentThreadId: UUID?,
    val authorId: UUID?,
    val isPrivate: Boolean,
    val categoryId: UUID?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val tags: List<String> = emptyList()
){
    companion object {
        fun from(entity: ThreadEntity): ThreadRes? =
            entity.id?.let {
                entity.board?.let { it1 ->
                    entity.author?.let { it2 ->
                        entity.tags?.let { tags ->
                            ThreadRes(
                                id = it,
                                boardId = it1.id,
                                authorId = it2.id,
                                title = entity.title,
                                createdAt = entity.createdAt,
                                content = entity.content,
                                parentThreadId = null,
                                isPrivate = entity.isPrivate,
                                categoryId = entity.categoryId,
                                updatedAt = entity.updatedAt,
                                tags = tags
                            )
                        }
                    }
                }
            }
    }
}


data class CommentRes(
    val id: UUID,
    val threadId: UUID,
    val authorId: UUID?,
    val parentId: UUID?,
    val content: String,
    val createdAt: Instant?,
    val isAuthor: Boolean = false // 게시물 작성자인지 여부
)

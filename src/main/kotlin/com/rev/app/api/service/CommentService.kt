package com.rev.app.api.service

import com.rev.app.api.service.community.dto.CommentDto
import com.rev.app.api.service.community.dto.ToggleResultDto
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class CommentService {

    fun addComment(
        threadId: Long,
        authorId: UUID,
        content: String,
        parentId: Long?      // ← 요구사항대로 Long?
    ): CommentDto {
        // TODO: 실제 저장 로직으로 교체
        return CommentDto(
            id = 1L,
            threadId = threadId,
            authorId = authorId,
            content = content,
            parentId = parentId,
            createdAt = Instant.now(),
            updatedAt = null
        )
    }

    fun toggleLike(commentId: Long, userId: UUID): ToggleResultDto {
        // TODO: 실제 토글 로직으로 교체
        return ToggleResultDto(
            toggled = true,
            count = 1L
        )
    }
}

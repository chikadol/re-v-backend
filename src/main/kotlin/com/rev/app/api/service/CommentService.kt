package com.rev.app.api.service

import com.rev.app.api.service.community.dto.CommentDto
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CommentService {
    fun addComment(
        threadId: Long,
        authorId: Long,
        content: String,
        parentId: Long?   // ← 여기 Long? 로
    ): CommentDto {
        // TODO: 실제 저장 로직. 지금은 컴파일용 더미
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

    fun toggleLike(commentId: Long, userId: Long): com.rev.app.api.service.community.dto.ToggleResultDto {
        // TODO: 실제 로직. 컴파일용 더미
        return com.rev.app.api.service.community.dto.ToggleResultDto(
            toggled = true,
            count = 1L
        )
    }
}

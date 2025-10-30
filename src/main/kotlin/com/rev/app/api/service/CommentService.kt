package com.rev.app.api.service

import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
) {
    fun addComment(
        threadId: Long,
        authorId: UUID,
        content: String,
        parentId: Long?
    ): CommentEntity {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { IllegalArgumentException("Thread not found: $threadId") }

        val author = userRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("User not found: $authorId") }

        val parent = parentId?.let {
            commentRepository.findById(it).orElseThrow { IllegalArgumentException("Parent comment not found: $it") }
        }

        val entity = CommentEntity(
            thread = thread,
            author = author,
            content = content,
            parent = parent
        )
        return commentRepository.save(entity)
    }

    // 좋아요 토글 등은 나중에 실제 로직으로 대체
    fun toggleLike(commentId: Long, userId: java.util.UUID): com.rev.app.api.service.community.dto.ToggleResultDto {
        // TODO: 실제 로직으로 교체
        return com.rev.app.api.service.community.dto.ToggleResultDto(
            toggled = true,
            count = 1L
        )
    }}
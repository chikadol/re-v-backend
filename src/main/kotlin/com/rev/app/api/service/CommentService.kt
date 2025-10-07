package com.rev.app.api.service

import com.rev.app.api.PageCursorResp
import com.rev.app.domain.community.Comment
import com.rev.app.domain.community.CommentReaction
import com.rev.app.domain.community.repo.CommentReactionRepository
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.util.CursorUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val commentReactionRepository: CommentReactionRepository
) {
    @Transactional
    fun createComment(authorId: Long, req: CreateCommentReq): Comment {
        val c = Comment(
            threadId = req.threadId,
            parentId = req.parentId,
            authorId = authorId,
            content = req.content,
            isAnonymous = req.isAnonymous,
            createdAt = Instant.now()
        )
        return commentRepository.save(c)
    }

    fun pageByThread(threadId: Long, size: Int, cursor: String?): PageCursorResp<Comment> {
        val cur = cursor?.let { CursorUtil.decode(it) }
        val items = commentRepository.pageByThreadKeysetAsc(
            threadId = threadId,
            cursorCreatedAt = cur?.createdAt,
            cursorId = cur?.id,
            pageable = PageRequest.of(0, size)
        )
        val next = items.lastOrNull()?.let { CursorUtil.encode(it.createdAt, it.id!!) }
        return PageCursorResp(items, next)
    }

    @Transactional
    fun reactComment(commentId: Long, userId: Long, req: CommentReactionReq): CommentReaction {
        val existing = commentReactionRepository.findByUserIdAndCommentId(userId, commentId)
        existing.forEach { commentReactionRepository.delete(it) }
        return commentReactionRepository.save(
            CommentReaction(commentId = commentId, userId = userId, kind = req.kind)
        )
    }
}

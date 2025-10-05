package com.rev.app.service.community

import com.rev.app.api.PageCursorResp
import com.rev.app.domain.community.*
import com.rev.app.domain.community.repo.CommentReactionRepository
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.util.CursorUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class CreateCommentReq(
    val threadId: Long,
    val content: String,
    val parentId: Long? = null,
    val isAnonymous: Boolean = false
)
data class CommentReactionReq(val kind: ReactionKind)

@Service
class CommentService(
    private val comments: CommentRepository,
    private val commentReactions: CommentReactionRepository
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
        return comments.save(c)
    }

    fun pageByThread(threadId: Long, size: Int, cursor: String?): PageCursorResp<Comment> {
        val c = cursor?.let { CursorUtil.decode(it) }
        val items = comments.pageByThreadKeysetAsc(threadId, c?.createdAt, c?.id, PageRequest.of(0, size))
        val next = items.lastOrNull()?.let { CursorUtil.encode(it.createdAt, it.id!!) }
        return PageCursorResp(items, next)
    }

    @Transactional
    fun reactComment(commentId: Long, userId: Long, req: CommentReactionReq): CommentReaction {
        val existing = commentReactions.findByUserIdAndCommentId(userId, commentId)
        existing.forEach { commentReactions.delete(it) }
        return commentReactions.save(CommentReaction(commentId = commentId, userId = userId, kind = req.kind))
    }
}

package com.rev.app.domain.community.repo

import com.rev.app.domain.community.CommentReaction
import org.springframework.data.jpa.repository.JpaRepository

interface CommentReactionRepository : JpaRepository<CommentReaction, Long> {
    fun findByUserIdAndCommentId(userId: Long, commentId: Long): List<CommentReaction>
}

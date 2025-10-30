
package com.rev.app.domain.community.repo

import com.rev.app.api.service.community.ReactionType
import com.rev.app.domain.community.ThreadReaction
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadReactionRepository : JpaRepository<ThreadReaction, Long> {

    fun countByThread_IdAndReactionType(
        threadId: Long,
        reactionType: ReactionType
    ): Long

    fun findAllByThread_IdAndUser_IdAndReactionType(
        threadId: Long,
        userId: java.util.UUID,
        reactionType: ReactionType
    ): List<ThreadReaction>
}


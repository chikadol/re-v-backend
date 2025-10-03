package com.rev.app.domain.community.repo

import com.rev.app.domain.community.ThreadReaction
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadReactionRepository : JpaRepository<ThreadReaction, Long> {
    fun findByUserIdAndThreadId(userId: Long, threadId: Long): List<ThreadReaction>
}

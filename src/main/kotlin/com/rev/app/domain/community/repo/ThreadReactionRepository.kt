package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.entity.ThreadReaction
import com.rev.app.api.service.community.ReactionType
import com.rev.app.auth.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ThreadReactionRepository : JpaRepository<ThreadReaction, Long> {

    fun findByThread_IdAndUser_Id(threadId: Long, userId: java.util.UUID): List<ThreadReaction>

    @Query("""
        select count(tr)
        from ThreadReaction tr
        where tr.thread.id = :threadId
          and tr.reaction = :reaction
    """)
    fun countByThreadAndReaction(threadId: Long, reaction: ReactionType): Long
}

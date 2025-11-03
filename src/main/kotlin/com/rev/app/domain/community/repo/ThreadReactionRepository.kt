package com.rev.app.domain.community.repo


import com.rev.app.api.service.community.ReactionType
import com.rev.app.domain.community.entity.ThreadReaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ThreadReactionRepository : JpaRepository<ThreadReaction, Long> {

    fun findByThread_IdAndUser_Id(threadId: Long, userId: UUID): List<ThreadReaction>

    fun deleteByThread_IdAndUser_IdAndReaction(threadId: Long, userId: UUID, reaction: ReactionType): Long

    @Query(
        """
        select count(tr) 
        from ThreadReaction tr
        where tr.thread.id = :threadId
          and tr.reaction = :reaction
        """
    )
    fun countByThreadAndReaction(
        @Param("threadId") threadId: Long,
        @Param("reaction") reaction: ReactionType
    ): Long
}

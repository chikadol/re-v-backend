package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Comment
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface CommentRepository : JpaRepository<Comment, Long> {
    @Query("""
        select c from Comment c
        where c.threadId = :threadId
          and (:cursorCreatedAt is null or (c.createdAt > :cursorCreatedAt or (c.createdAt = :cursorCreatedAt and c.id > :cursorId)))
        order by c.createdAt asc, c.id asc
    """)
    fun pageByThreadKeysetAsc(
        threadId: Long,
        cursorCreatedAt: Instant?,
        cursorId: Long?,
        pageable: Pageable
    ): List<Comment>
}

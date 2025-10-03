package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Thread
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ThreadRepository : JpaRepository<Thread, Long> {

    @Query("""
select t from Thread t where t.boardId = :boardId
  and (:cursorCreatedAt is null or (t.createdAt < :cursorCreatedAt or (t.createdAt = :cursorCreatedAt and t.id < :cursorId)))
order by t.createdAt desc, t.id desc
""")
    fun pageByBoardKeyset(boardId: Long, cursorCreatedAt: Instant?, cursorId: Long?, pageable: Pageable): List<Thread>

    @Query("""
select max(t.displayNo) from Thread t where t.boardId = :boardId
""")
    fun findMaxDisplayNo(boardId: Long): Long?
}

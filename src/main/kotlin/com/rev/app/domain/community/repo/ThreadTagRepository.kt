package com.rev.app.domain.community.repo

import com.rev.app.domain.community.model.ThreadTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ThreadTagRepository : JpaRepository<ThreadTagEntity, UUID> {
    fun findByThread_Id(threadId: UUID): List<ThreadTagEntity>
    
    // 배치 조회로 N+1 문제 해결
    @Query(
        """
        select tt from ThreadTagEntity tt
        join fetch tt.tag t
        where tt.thread.id in :threadIds
        """
    )
    fun findByThread_IdIn(@Param("threadIds") threadIds: List<UUID>): List<ThreadTagEntity>

    @Modifying
    @Query("delete from ThreadTagEntity t where t.thread.id = :threadId")
    fun deleteByThreadId(threadId: UUID)
}
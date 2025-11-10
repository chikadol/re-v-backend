package com.rev.app.domain.community.repo

import com.rev.app.domain.community.model.ThreadTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ThreadTagRepository : JpaRepository<ThreadTagEntity, UUID> {
    fun findByThread_Id(threadId: UUID): List<ThreadTagEntity>

    @Modifying
    @Query("delete from ThreadTagEntity t where t.thread.id = :threadId")
    fun deleteByThreadId(threadId: UUID)
}
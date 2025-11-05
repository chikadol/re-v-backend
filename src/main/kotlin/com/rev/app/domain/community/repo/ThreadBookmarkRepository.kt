package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {
    fun existsByUser_IdAndThread_Id(userId: UUID, threadId: UUID): Boolean
    fun deleteByUser_IdAndThread_Id(userId: UUID, threadId: UUID)
    fun countByThread_Id(threadId: UUID): Long
}

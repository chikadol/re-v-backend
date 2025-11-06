package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {
    fun countByThread_Id(threadId: UUID): Long
    fun existsByThread_IdAndUser_Id(threadId: UUID, userId: UUID): Boolean
    fun deleteByThread_IdAndUser_Id(threadId: UUID, userId: UUID)
    fun findByThread_IdAndUser_Id(threadId: UUID, userId: UUID): ThreadBookmarkEntity?
}

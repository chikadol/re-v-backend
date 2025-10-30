package com.rev.app.domain.community.repo


import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, Long> {
    fun existsByUser_IdAndThread_Id(userId: UUID, threadId: Long): Boolean
    fun countByThread_Id(threadId: Long): Long
    fun deleteByUser_IdAndThread_Id(userId: UUID, threadId: Long): Long
}

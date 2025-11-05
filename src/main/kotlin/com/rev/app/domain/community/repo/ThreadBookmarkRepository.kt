package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {

    // 🔁 모두 UUID 로 변경
    fun countByThread_Id(threadId: UUID): Long

    fun existsByThread_IdAndUser_Id(threadId: UUID, userId: UUID): Boolean

    fun deleteByThread_IdAndUser_Id(threadId: UUID, userId: UUID): Long

    // 필요하면 이런 것도 전부 UUID
    fun findAllByThread_Id(threadId: UUID): List<ThreadBookmarkEntity>
}

package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, Long> {
    fun findByThread_IdAndUser_Id(threadId: Long, userId: UUID): ThreadBookmarkEntity?
}

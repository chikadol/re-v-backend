package com.rev.app.domain.community.repo

import com.rev.app.domain.community.model.ThreadReactionEntity   // ✅ model 패키지로 통일
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadReactionRepository : JpaRepository<ThreadReactionEntity, Long> {
    fun findByThread_IdAndUser_IdAndType(threadId: UUID, userId: UUID, type: String): ThreadReactionEntity?
    fun countByThread_IdAndType(threadId: UUID, type: String): Long
    fun findAllByThread_Id(threadId: UUID): List<ThreadReactionEntity>
}

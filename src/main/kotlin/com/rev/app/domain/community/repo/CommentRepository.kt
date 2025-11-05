package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.CommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CommentRepository : JpaRepository<CommentEntity, UUID> {
    fun findAllByThreadId(threadId: UUID): List<CommentEntity>
}

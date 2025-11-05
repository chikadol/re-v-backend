package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<com.rev.app.domain.community.entity.ThreadEntity, UUID> {
    fun findByBoard_IdAndIsPrivateFalseOrderByCreatedAtDesc(boardId: UUID, pageable: Pageable): Page<ThreadEntity>
    fun findByBoard_IdAndIsPrivateFalseOrderByCreatedAtDesc(boardId: UUID): List<ThreadEntity>
}

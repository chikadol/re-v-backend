package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<ThreadEntity, UUID> {
    fun findAllByBoard_IdAndIsPrivateFalse(boardId: Long, pageable: Pageable): Page<ThreadEntity>
}

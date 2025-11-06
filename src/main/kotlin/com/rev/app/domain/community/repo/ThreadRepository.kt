package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ThreadRepository : JpaRepository<ThreadEntity, UUID> {
    @EntityGraph(attributePaths = ["author", "board"])
    fun findByBoard_IdAndIsPrivateFalse(boardId: UUID, pageable: Pageable): Page<ThreadEntity>
}


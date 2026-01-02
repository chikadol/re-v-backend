package com.rev.app.domain.community.repo

import com.rev.app.domain.community.BoardRequestEntity
import com.rev.app.domain.community.BoardRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BoardRequestRepository : JpaRepository<BoardRequestEntity, UUID> {
    fun findAllByStatusOrderByCreatedAtDesc(status: BoardRequestStatus, pageable: Pageable): Page<BoardRequestEntity>
    fun findAllByRequester_IdOrderByCreatedAtDesc(requesterId: UUID, pageable: Pageable): Page<BoardRequestEntity>
    fun countByStatus(status: BoardRequestStatus): Long
}


package com.rev.app.domain.ticket.repo

import com.rev.app.domain.ticket.entity.TicketEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TicketRepository : JpaRepository<TicketEntity, UUID> {
    fun findAllByUser_IdOrderByPurchaseDateDesc(userId: UUID, pageable: Pageable): Page<TicketEntity>
    fun countByPerformance_Id(performanceId: UUID): Long
}


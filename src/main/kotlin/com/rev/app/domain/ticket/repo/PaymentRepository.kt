package com.rev.app.domain.ticket.repo

import com.rev.app.domain.ticket.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PaymentRepository : JpaRepository<PaymentEntity, UUID> {
    fun findByTicket_Id(ticketId: UUID): PaymentEntity?
}


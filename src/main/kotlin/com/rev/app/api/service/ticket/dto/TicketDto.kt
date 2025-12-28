package com.rev.app.api.service.ticket.dto

import com.rev.app.domain.ticket.entity.TicketEntity
import com.rev.app.domain.ticket.entity.TicketStatus
import java.time.Instant
import java.util.UUID

data class TicketRes(
    val id: UUID,
    val performanceId: UUID,
    val performanceTitle: String,
    val performanceDateTime: java.time.LocalDateTime,
    val venue: String,
    val price: Int,
    val seatNumber: String?,
    val status: TicketStatus,
    val purchaseDate: Instant?
) {
    companion object {
        fun from(entity: TicketEntity): TicketRes {
            val performance = entity.performance!!
            return TicketRes(
                id = entity.id!!,
                performanceId = performance.id!!,
                performanceTitle = performance.title,
                performanceDateTime = performance.performanceDateTime,
                venue = performance.venue,
                price = entity.price,
                seatNumber = entity.seatNumber,
                status = entity.status,
                purchaseDate = entity.purchaseDate
            )
        }
    }
}

data class TicketPurchaseRequest(
    val performanceId: UUID,
    val quantity: Int = 1,
    val seatNumber: String? = null
)


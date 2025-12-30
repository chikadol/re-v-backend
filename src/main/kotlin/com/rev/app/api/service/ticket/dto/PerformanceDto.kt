package com.rev.app.api.service.ticket.dto

import com.rev.app.domain.ticket.entity.PerformanceEntity
import com.rev.app.domain.ticket.entity.PerformanceStatus
import java.time.LocalDateTime
import java.util.UUID

data class PerformanceRes(
    val id: UUID,
    val title: String,
    val description: String?,
    val venue: String,
    val performanceDateTime: LocalDateTime,
    val price: Int,
    val advPrice: Int?,
    val doorPrice: Int?,
    val totalSeats: Int,
    val remainingSeats: Int,
    val imageUrl: String?,
    val status: PerformanceStatus,
    val idolId: UUID?,
    val performers: List<String> = emptyList()
) {
    companion object {
        fun from(entity: PerformanceEntity): PerformanceRes = PerformanceRes(
            id = entity.id!!,
            title = entity.title,
            description = entity.description,
            venue = entity.venue,
            performanceDateTime = entity.performanceDateTime,
            price = entity.price,
            advPrice = entity.advPrice,
            doorPrice = entity.doorPrice,
            totalSeats = entity.totalSeats,
            remainingSeats = entity.remainingSeats,
            imageUrl = entity.imageUrl,
            status = entity.status,
            idolId = entity.idol?.id,
            performers = entity.performers.toList()
        )
    }
}

data class PerformanceCreateRequest(
    val title: String,
    val description: String? = null,
    val venue: String,
    val performanceDateTime: LocalDateTime,
    val price: Int? = null,
    val advPrice: Int? = null,
    val doorPrice: Int? = null,
    val totalSeats: Int,
    val imageUrl: String? = null,
    val idolId: UUID? = null,
    val performers: List<String> = emptyList()
)


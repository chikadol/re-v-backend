package com.rev.app.api.service.ticket

import com.rev.app.api.service.ticket.dto.PerformanceCreateRequest
import com.rev.app.api.service.ticket.dto.PerformanceRes
import com.rev.app.domain.ticket.entity.PerformanceEntity
import com.rev.app.domain.ticket.entity.PerformanceStatus
import com.rev.app.domain.ticket.repo.PerformanceRepository
import com.rev.app.domain.idol.IdolRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val idolRepository: IdolRepository
) {
    @Transactional(readOnly = true)
    fun getAll(): List<PerformanceRes> {
        return performanceRepository.findAllByOrderByPerformanceDateTimeAsc()
            .map { PerformanceRes.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): PerformanceRes {
        val performance = performanceRepository.findById(id)
            .orElseThrow { IllegalArgumentException("공연을 찾을 수 없습니다: $id") }
        return PerformanceRes.from(performance)
    }

    @Transactional
    fun create(request: PerformanceCreateRequest): PerformanceRes {
        val idol = request.idolId?.let { idolRepository.findById(it).orElse(null) }
        val effectivePrice = request.advPrice ?: request.doorPrice ?: request.price ?: 30000
        val performance = PerformanceEntity(
            title = request.title,
            description = request.description,
            venue = request.venue,
            performanceDateTime = request.performanceDateTime,
            price = effectivePrice,
            advPrice = request.advPrice,
            doorPrice = request.doorPrice,
            totalSeats = request.totalSeats,
            remainingSeats = request.totalSeats,
            imageUrl = request.imageUrl,
            status = PerformanceStatus.UPCOMING,
            idol = idol,
            performers = request.performers.toMutableList()
        )
        val saved = performanceRepository.saveAndFlush(performance)
        return PerformanceRes.from(saved)
    }

    @Transactional
    fun updateStatus(id: UUID, status: PerformanceStatus) {
        val performance = performanceRepository.findById(id)
            .orElseThrow { IllegalArgumentException("공연을 찾을 수 없습니다: $id") }
        performance.status = status
        performanceRepository.save(performance)
    }

    @Transactional(readOnly = true)
    fun getUpcoming(): List<PerformanceRes> {
        return performanceRepository.findAllByStatusOrderByPerformanceDateTimeAsc(PerformanceStatus.UPCOMING)
            .map { PerformanceRes.from(it) }
    }

    @Transactional(readOnly = true)
    fun getByIdol(idolId: UUID): List<PerformanceRes> =
        performanceRepository.findAllByIdol_IdOrderByPerformanceDateTimeAsc(idolId).map { PerformanceRes.from(it) }
}


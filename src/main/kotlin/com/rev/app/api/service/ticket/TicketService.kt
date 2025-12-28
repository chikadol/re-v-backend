package com.rev.app.api.service.ticket

import com.rev.app.api.service.ticket.dto.TicketPurchaseRequest
import com.rev.app.api.service.ticket.dto.TicketRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.ticket.entity.TicketEntity
import com.rev.app.domain.ticket.entity.TicketStatus
import com.rev.app.domain.ticket.repo.PerformanceRepository
import com.rev.app.domain.ticket.repo.TicketRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val performanceRepository: PerformanceRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun purchase(userId: UUID, request: TicketPurchaseRequest): TicketRes {
        val performance = performanceRepository.findById(request.performanceId)
            .orElseThrow { IllegalArgumentException("공연을 찾을 수 없습니다: ${request.performanceId}") }

        if (performance.remainingSeats < request.quantity) {
            throw IllegalArgumentException("남은 좌석이 부족합니다. (남은 좌석: ${performance.remainingSeats})")
        }

        val user = userRepository.getReferenceById(userId)

        val ticket = TicketEntity(
            performance = performance,
            user = user,
            price = performance.price * request.quantity,
            seatNumber = request.seatNumber,
            status = TicketStatus.PENDING
        )

        // 좌석 수 감소
        performance.remainingSeats -= request.quantity
        performanceRepository.save(performance)

        val saved = ticketRepository.saveAndFlush(ticket)
        return TicketRes.from(saved)
    }

    @Transactional(readOnly = true)
    fun getMyTickets(userId: UUID, pageable: Pageable): Page<TicketRes> {
        return ticketRepository.findAllByUser_IdOrderByPurchaseDateDesc(userId, pageable)
            .map { TicketRes.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(ticketId: UUID, userId: UUID): TicketRes {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { IllegalArgumentException("티켓을 찾을 수 없습니다: $ticketId") }

        if (ticket.user?.id != userId) {
            throw IllegalArgumentException("본인의 티켓만 조회할 수 있습니다.")
        }

        return TicketRes.from(ticket)
    }

    @Transactional
    fun confirmTicket(ticketId: UUID, userId: UUID) {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { IllegalArgumentException("티켓을 찾을 수 없습니다: $ticketId") }

        if (ticket.user?.id != userId) {
            throw IllegalArgumentException("본인의 티켓만 확정할 수 있습니다.")
        }

        ticket.status = TicketStatus.CONFIRMED
        ticketRepository.save(ticket)
    }

    @Transactional
    fun cancelTicket(ticketId: UUID, userId: UUID) {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { IllegalArgumentException("티켓을 찾을 수 없습니다: $ticketId") }

        if (ticket.user?.id != userId) {
            throw IllegalArgumentException("본인의 티켓만 취소할 수 있습니다.")
        }

        ticket.status = TicketStatus.CANCELLED
        val performance = ticket.performance!!
        performance.remainingSeats += 1 // 좌석 복구
        performanceRepository.save(performance)
        ticketRepository.save(ticket)
    }
}


package com.rev.app.api.service.ticket

import com.rev.app.api.service.ticket.dto.PaymentRequest
import com.rev.app.api.service.ticket.dto.PaymentRes
import com.rev.app.domain.ticket.entity.PaymentEntity
import com.rev.app.domain.ticket.entity.PaymentMethod
import com.rev.app.domain.ticket.entity.PaymentStatus
import com.rev.app.domain.ticket.entity.TicketStatus
import com.rev.app.domain.ticket.repo.PaymentRepository
import com.rev.app.domain.ticket.repo.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val ticketRepository: TicketRepository
) {
    @Transactional
    fun createPayment(request: PaymentRequest, userId: UUID): PaymentRes {
        val ticket = ticketRepository.findById(request.ticketId)
            .orElseThrow { IllegalArgumentException("티켓을 찾을 수 없습니다: ${request.ticketId}") }

        if (ticket.user?.id != userId) {
            throw IllegalArgumentException("본인의 티켓만 결제할 수 있습니다.")
        }

        if (ticket.status != TicketStatus.PENDING) {
            throw IllegalArgumentException("결제 대기 중인 티켓만 결제할 수 있습니다.")
        }

        // 이미 결제가 있는지 확인
        val existingPayment = paymentRepository.findByTicket_Id(request.ticketId)
        if (existingPayment != null && existingPayment.status == PaymentStatus.COMPLETED) {
            throw IllegalArgumentException("이미 결제가 완료된 티켓입니다.")
        }

        val payment = PaymentEntity(
            ticket = ticket,
            amount = ticket.price,
            paymentMethod = request.paymentMethod,
            status = PaymentStatus.PENDING
        )

        val saved = paymentRepository.saveAndFlush(payment)

        // 실제 결제 처리는 외부 API를 호출해야 하지만, 여기서는 시뮬레이션
        // 실제로는 네이버페이, 토스, 카카오페이 API를 호출해야 함
        completePayment(saved.id!!)

        return PaymentRes(
            id = saved.id!!,
            ticketId = ticket.id!!,
            amount = saved.amount,
            paymentMethod = saved.paymentMethod,
            status = saved.status
        )
    }

    @Transactional
    fun completePayment(paymentId: UUID) {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("결제를 찾을 수 없습니다: $paymentId") }

        payment.status = PaymentStatus.COMPLETED
        payment.paidAt = Instant.now()
        payment.paymentId = "PAY_${paymentId.toString().replace("-", "").substring(0, 16)}" // 시뮬레이션용 ID

        val ticket = payment.ticket!!
        ticket.status = TicketStatus.CONFIRMED

        paymentRepository.save(payment)
        ticketRepository.save(ticket)
    }

    @Transactional(readOnly = true)
    fun getByTicketId(ticketId: UUID): PaymentRes? {
        return paymentRepository.findByTicket_Id(ticketId)?.let {
            PaymentRes(
                id = it.id!!,
                ticketId = it.ticket!!.id!!,
                amount = it.amount,
                paymentMethod = it.paymentMethod,
                status = it.status
            )
        }
    }
}


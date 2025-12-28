package com.rev.app.api.service.ticket

import com.rev.app.api.service.payment.*
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
    private val ticketRepository: TicketRepository,
    private val paymentProviders: List<PaymentProvider>
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

        // 기존 결제가 있으면 재사용, 없으면 새로 생성
        val payment = existingPayment ?: PaymentEntity(
            ticket = ticket,
            amount = ticket.price,
            paymentMethod = request.paymentMethod,
            status = PaymentStatus.PENDING
        )

        val saved = paymentRepository.saveAndFlush(payment)

        // 해당 결제 방법에 맞는 Provider 찾기
        val provider = paymentProviders.find { it.supportedMethod == request.paymentMethod }
            ?: throw IllegalArgumentException("지원하지 않는 결제 방법입니다: ${request.paymentMethod}")

        // 실제 결제 API 호출하여 결제 URL 생성
        val performance = ticket.performance
        val user = ticket.user
        val paymentResponse = provider.createPayment(
            orderId = saved.id!!.toString(),
            amount = saved.amount,
            itemName = performance?.title ?: "공연 티켓",
            customerName = user?.username ?: "고객",
            customerEmail = user?.email,
            customerPhone = null
        )

        if (!paymentResponse.success || paymentResponse.paymentUrl == null) {
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            throw IllegalArgumentException(paymentResponse.errorMessage ?: "결제 요청에 실패했습니다.")
        }

        // paymentKey 저장
        if (paymentResponse.paymentKey != null) {
            payment.paymentId = paymentResponse.paymentKey
            paymentRepository.save(payment)
        }

        // 결제 URL을 반환하기 위해 PaymentRes에 paymentUrl 필드 추가 필요
        // 여기서는 기존 구조 유지하고, 별도 엔드포인트로 결제 URL 반환
        return PaymentRes(
            id = saved.id!!,
            ticketId = ticket.id!!,
            amount = saved.amount,
            paymentMethod = saved.paymentMethod,
            status = saved.status,
            paymentUrl = paymentResponse.paymentUrl
        )
    }

    @Transactional
    fun approvePayment(paymentId: UUID, paymentKey: String, orderId: String): PaymentRes {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("결제를 찾을 수 없습니다: $paymentId") }

        if (payment.status == PaymentStatus.COMPLETED) {
            return PaymentRes(
                id = payment.id!!,
                ticketId = payment.ticket!!.id!!,
                amount = payment.amount,
                paymentMethod = payment.paymentMethod,
                status = payment.status
            )
        }

        // 해당 결제 방법에 맞는 Provider 찾기
        val provider = paymentProviders.find { it.supportedMethod == payment.paymentMethod }
            ?: throw IllegalArgumentException("지원하지 않는 결제 방법입니다: ${payment.paymentMethod}")

        // 실제 결제 승인 API 호출
        val approvalResponse = provider.approvePayment(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = payment.amount
        )

        if (!approvalResponse.success) {
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            throw IllegalArgumentException(approvalResponse.errorMessage ?: "결제 승인에 실패했습니다.")
        }

        // 결제 완료 처리
        payment.status = PaymentStatus.COMPLETED
        payment.paidAt = if (approvalResponse.paidAt != null) {
            Instant.ofEpochSecond(approvalResponse.paidAt)
        } else {
            Instant.now()
        }
        payment.paymentId = paymentKey

        val ticket = payment.ticket!!
        ticket.status = TicketStatus.CONFIRMED

        paymentRepository.save(payment)
        ticketRepository.save(ticket)

        return PaymentRes(
            id = payment.id!!,
            ticketId = ticket.id!!,
            amount = payment.amount,
            paymentMethod = payment.paymentMethod,
            status = payment.status
        )
    }

    @Transactional
    fun cancelPayment(paymentId: UUID, cancelReason: String): PaymentRes {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("결제를 찾을 수 없습니다: $paymentId") }

        if (payment.status != PaymentStatus.COMPLETED) {
            throw IllegalArgumentException("완료된 결제만 취소할 수 있습니다.")
        }

        val paymentKey = payment.paymentId
            ?: throw IllegalArgumentException("결제 키가 없습니다.")

        // 해당 결제 방법에 맞는 Provider 찾기
        val provider = paymentProviders.find { it.supportedMethod == payment.paymentMethod }
            ?: throw IllegalArgumentException("지원하지 않는 결제 방법입니다: ${payment.paymentMethod}")

        // 실제 결제 취소 API 호출
        val cancelResponse = provider.cancelPayment(
            paymentKey = paymentKey,
            cancelReason = cancelReason
        )

        if (!cancelResponse.success) {
            throw IllegalArgumentException(cancelResponse.errorMessage ?: "결제 취소에 실패했습니다.")
        }

        // 결제 취소 처리
        payment.status = PaymentStatus.CANCELLED

        val ticket = payment.ticket!!
        ticket.status = TicketStatus.CANCELLED

        paymentRepository.save(payment)
        ticketRepository.save(ticket)

        return PaymentRes(
            id = payment.id!!,
            ticketId = ticket.id!!,
            amount = payment.amount,
            paymentMethod = payment.paymentMethod,
            status = payment.status
        )
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


package com.rev.app.api.service.ticket.dto

import com.rev.app.domain.ticket.entity.PaymentMethod
import com.rev.app.domain.ticket.entity.PaymentStatus
import java.util.UUID

data class PaymentRes(
    val id: UUID,
    val ticketId: UUID,
    val amount: Int,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val paymentUrl: String? = null // 결제 URL (결제 제공자로 리다이렉트)
)

data class PaymentRequest(
    val ticketId: UUID,
    val paymentMethod: PaymentMethod
)


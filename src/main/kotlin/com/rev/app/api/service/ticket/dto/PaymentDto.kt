package com.rev.app.api.service.ticket.dto

import com.rev.app.domain.ticket.entity.PaymentMethod
import com.rev.app.domain.ticket.entity.PaymentStatus
import java.util.UUID

data class PaymentRes(
    val id: UUID,
    val ticketId: UUID,
    val amount: Int,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus
)

data class PaymentRequest(
    val ticketId: UUID,
    val paymentMethod: PaymentMethod
)


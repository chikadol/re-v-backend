package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.ticket.PaymentService
import com.rev.app.api.service.ticket.dto.PaymentRequest
import com.rev.app.api.service.ticket.dto.PaymentRes
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPayment(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @Valid @RequestBody request: PaymentRequest
    ): PaymentRes {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return paymentService.createPayment(request, userId)
    }

    @GetMapping("/ticket/{ticketId}")
    fun getByTicketId(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable ticketId: UUID
    ): PaymentRes? {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return paymentService.getByTicketId(ticketId)
    }
}


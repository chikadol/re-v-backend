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

    @PostMapping("/{paymentId}/approve")
    fun approvePayment(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable paymentId: UUID,
        @RequestParam paymentKey: String,
        @RequestParam orderId: String
    ): PaymentRes {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return paymentService.approvePayment(paymentId, paymentKey, orderId)
    }

    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable paymentId: UUID,
        @RequestParam(required = false, defaultValue = "사용자 요청") cancelReason: String
    ): PaymentRes {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return paymentService.cancelPayment(paymentId, cancelReason)
    }

    @GetMapping("/ticket/{ticketId}")
    fun getByTicketId(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable ticketId: UUID
    ): PaymentRes? {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return paymentService.getByTicketId(ticketId)
    }

    @GetMapping("/{paymentId}")
    fun getById(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable paymentId: UUID
    ): PaymentRes? {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return paymentService.getById(paymentId)
    }

    /**
     * 결제 콜백 처리 (결제 제공자에서 리다이렉트)
     * 각 결제 서비스에서 결제 완료 후 이 엔드포인트로 리다이렉트됩니다.
     */
    @GetMapping("/callback")
    fun paymentCallback(
        @RequestParam(required = false) method: String?,
        @RequestParam(required = false) success: String?,
        @RequestParam(required = false) paymentKey: String?,
        @RequestParam(required = false) orderId: String?,
        @RequestParam(required = false) pg_token: String? // 카카오페이용
    ): Map<String, Any> {
        // 결제 성공 시 자동 승인 처리
        if (success == "true" && paymentKey != null && orderId != null) {
            try {
                val paymentId = UUID.fromString(orderId)
                paymentService.approvePayment(paymentId, paymentKey, orderId)
                return mapOf(
                    "success" to true,
                    "message" to "결제가 완료되었습니다.",
                    "redirectUrl" to "/my-tickets"
                )
            } catch (e: Exception) {
                return mapOf(
                    "success" to false,
                    "message" to "결제 승인 처리 중 오류가 발생했습니다: ${e.message}",
                    "redirectUrl" to "/my-tickets"
                )
            }
        }

        return mapOf(
            "success" to false,
            "message" to "결제가 취소되었거나 실패했습니다.",
            "redirectUrl" to "/my-tickets"
        )
    }
}


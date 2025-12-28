package com.rev.app.api.service.payment

import com.rev.app.domain.ticket.entity.PaymentMethod
import java.util.UUID

/**
 * 결제 제공자 인터페이스
 * 각 결제 서비스(네이버페이, 토스, 카카오페이)는 이 인터페이스를 구현합니다.
 */
interface PaymentProvider {
    /**
     * 결제 요청을 생성하고 결제 URL을 반환합니다.
     * @param orderId 주문 ID (결제 고유 ID)
     * @param amount 결제 금액
     * @param itemName 상품명
     * @param customerName 고객명
     * @param customerEmail 고객 이메일
     * @param customerPhone 고객 전화번호
     * @return 결제 URL (사용자가 리다이렉트될 URL)
     */
    fun createPayment(
        orderId: String,
        amount: Int,
        itemName: String,
        customerName: String,
        customerEmail: String?,
        customerPhone: String?
    ): PaymentResponse

    /**
     * 결제 승인을 처리합니다.
     * @param paymentKey 결제 키 (각 서비스에서 제공)
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 결제 승인 결과
     */
    fun approvePayment(
        paymentKey: String,
        orderId: String,
        amount: Int
    ): PaymentApprovalResponse

    /**
     * 결제를 취소합니다.
     * @param paymentKey 결제 키
     * @param cancelReason 취소 사유
     * @return 취소 결과
     */
    fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ): PaymentCancelResponse

    /**
     * 결제 상태를 조회합니다.
     * @param paymentKey 결제 키
     * @return 결제 상태 정보
     */
    fun getPaymentStatus(paymentKey: String): PaymentStatusResponse

    /**
     * 이 제공자가 지원하는 결제 방법
     */
    val supportedMethod: PaymentMethod
}

/**
 * 결제 요청 응답
 */
data class PaymentResponse(
    val success: Boolean,
    val paymentUrl: String? = null,
    val paymentKey: String? = null,
    val errorMessage: String? = null
)

/**
 * 결제 승인 응답
 */
data class PaymentApprovalResponse(
    val success: Boolean,
    val paymentKey: String? = null,
    val orderId: String? = null,
    val amount: Int? = null,
    val paidAt: Long? = null, // Unix timestamp
    val errorMessage: String? = null
)

/**
 * 결제 취소 응답
 */
data class PaymentCancelResponse(
    val success: Boolean,
    val cancelAmount: Int? = null,
    val canceledAt: Long? = null, // Unix timestamp
    val errorMessage: String? = null
)

/**
 * 결제 상태 조회 응답
 */
data class PaymentStatusResponse(
    val status: String, // PENDING, COMPLETED, FAILED, CANCELLED
    val paymentKey: String? = null,
    val orderId: String? = null,
    val amount: Int? = null,
    val paidAt: Long? = null
)


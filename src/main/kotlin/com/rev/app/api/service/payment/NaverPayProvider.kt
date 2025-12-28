package com.rev.app.api.service.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.rev.app.domain.ticket.entity.PaymentMethod
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.*

/**
 * 네이버페이 결제 제공자
 * 네이버페이 API 문서: https://developers.pay.naver.com/docs/v2/api
 */
@Component
class NaverPayProvider(
    @Value("\${payment.naver.client-id:}")
    private val clientId: String,
    @Value("\${payment.naver.client-secret:}")
    private val clientSecret: String,
    @Value("\${payment.naver.api-url:https://dev.apis.naver.com}")
    private val apiUrl: String,
    @Value("\${payment.return-url:http://localhost:3000/payment/callback}")
    private val returnUrl: String,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) : PaymentProvider {
    private val logger = LoggerFactory.getLogger(NaverPayProvider::class.java)

    override val supportedMethod: PaymentMethod = PaymentMethod.NAVER_PAY

    override fun createPayment(
        orderId: String,
        amount: Int,
        itemName: String,
        customerName: String,
        customerEmail: String?,
        customerPhone: String?
    ): PaymentResponse {
        try {
            logger.info("네이버페이 결제 요청: orderId=$orderId, amount=$amount, itemName=$itemName")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Naver-Client-Id", clientId)
                set("X-Naver-Client-Secret", clientSecret)
            }

            val requestBody = mapOf(
                "orderId" to orderId,
                "amount" to amount,
                "productName" to itemName,
                "customerName" to customerName,
                "customerEmail" to (customerEmail ?: ""),
                "customerPhone" to (customerPhone ?: ""),
                "returnUrl" to returnUrl
            )

            val request = HttpEntity(requestBody, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/naverpay/payments/v1/payment",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val paymentUrl = body["paymentUrl"] as? String
                val paymentKey = body["paymentKey"] as? String

                if (paymentUrl != null) {
                    logger.info("네이버페이 결제 URL 생성 성공: $paymentUrl")
                    return PaymentResponse(
                        success = true,
                        paymentUrl = paymentUrl,
                        paymentKey = paymentKey
                    )
                }
            }

            logger.warn("네이버페이 결제 요청 실패: ${response.statusCode}")
            return PaymentResponse(
                success = false,
                errorMessage = "네이버페이 결제 요청에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("네이버페이 결제 요청 중 오류 발생", e)
            return PaymentResponse(
                success = false,
                errorMessage = "네이버페이 결제 요청 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun approvePayment(
        paymentKey: String,
        orderId: String,
        amount: Int
    ): PaymentApprovalResponse {
        try {
            logger.info("네이버페이 결제 승인: paymentKey=$paymentKey, orderId=$orderId")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Naver-Client-Id", clientId)
                set("X-Naver-Client-Secret", clientSecret)
            }

            val requestBody = mapOf(
                "paymentKey" to paymentKey,
                "orderId" to orderId,
                "amount" to amount
            )

            val request = HttpEntity(requestBody, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/naverpay/payments/v1/payment/approve",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val approvedAmount = (body["totalAmount"] as? Number)?.toInt() ?: amount
                val approvedAt = (body["approvedAt"] as? String)?.let {
                    Instant.parse(it).epochSecond
                } ?: Instant.now().epochSecond

                logger.info("네이버페이 결제 승인 성공: paymentKey=$paymentKey")
                return PaymentApprovalResponse(
                    success = true,
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = approvedAmount,
                    paidAt = approvedAt
                )
            }

            logger.warn("네이버페이 결제 승인 실패: ${response.statusCode}")
            return PaymentApprovalResponse(
                success = false,
                errorMessage = "네이버페이 결제 승인에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("네이버페이 결제 승인 중 오류 발생", e)
            return PaymentApprovalResponse(
                success = false,
                errorMessage = "네이버페이 결제 승인 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ): PaymentCancelResponse {
        try {
            logger.info("네이버페이 결제 취소: paymentKey=$paymentKey")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Naver-Client-Id", clientId)
                set("X-Naver-Client-Secret", clientSecret)
            }

            val requestBody = mapOf(
                "paymentKey" to paymentKey,
                "cancelReason" to cancelReason
            )

            val request = HttpEntity(requestBody, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/naverpay/payments/v1/payment/cancel",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val cancelAmount = (body["cancelAmount"] as? Number)?.toInt()
                val canceledAt = Instant.now().epochSecond

                logger.info("네이버페이 결제 취소 성공: paymentKey=$paymentKey")
                return PaymentCancelResponse(
                    success = true,
                    cancelAmount = cancelAmount,
                    canceledAt = canceledAt
                )
            }

            logger.warn("네이버페이 결제 취소 실패: ${response.statusCode}")
            return PaymentCancelResponse(
                success = false,
                errorMessage = "네이버페이 결제 취소에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("네이버페이 결제 취소 중 오류 발생", e)
            return PaymentCancelResponse(
                success = false,
                errorMessage = "네이버페이 결제 취소 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun getPaymentStatus(paymentKey: String): PaymentStatusResponse {
        try {
            logger.info("네이버페이 결제 상태 조회: paymentKey=$paymentKey")

            val headers = HttpHeaders().apply {
                set("X-Naver-Client-Id", clientId)
                set("X-Naver-Client-Secret", clientSecret)
            }

            val request = HttpEntity<Void>(headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/naverpay/payments/v1/payment/$paymentKey",
                HttpMethod.GET,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val status = when (body["status"] as? String) {
                    "PAYMENT_COMPLETED" -> "COMPLETED"
                    "PAYMENT_FAILED" -> "FAILED"
                    "PAYMENT_CANCELED" -> "CANCELLED"
                    else -> "PENDING"
                }
                val orderId = body["orderId"] as? String
                val amount = (body["totalAmount"] as? Number)?.toInt()
                val paidAt = (body["approvedAt"] as? String)?.let {
                    Instant.parse(it).epochSecond
                }

                return PaymentStatusResponse(
                    status = status,
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = amount,
                    paidAt = paidAt
                )
            }

            return PaymentStatusResponse(
                status = "PENDING",
                paymentKey = paymentKey
            )
        } catch (e: Exception) {
            logger.error("네이버페이 결제 상태 조회 중 오류 발생", e)
            return PaymentStatusResponse(
                status = "PENDING",
                paymentKey = paymentKey
            )
        }
    }
}


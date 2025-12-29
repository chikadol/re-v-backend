package com.rev.app.api.service.payment

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
 * 토스페이먼츠 결제 제공자
 * 토스페이먼츠 API 문서: https://docs.tosspayments.com/
 */
@Component
class TossPayProvider(
    @Value("\${payment.toss.secret-key:}")
    private val secretKey: String,
    @Value("\${payment.toss.api-url:https://api.tosspayments.com}")
    private val apiUrl: String,
    @Value("\${payment.return-url:http://localhost:5173/payment/callback}")
    private val returnUrl: String,
    private val restTemplate: RestTemplate
) : PaymentProvider {
    private val logger = LoggerFactory.getLogger(TossPayProvider::class.java)

    override val supportedMethod: PaymentMethod = PaymentMethod.TOSS

    override fun createPayment(
        orderId: String,
        amount: Int,
        itemName: String,
        customerName: String,
        customerEmail: String?,
        customerPhone: String?
    ): PaymentResponse {
        try {
            logger.info("토스페이먼츠 결제 요청: orderId=$orderId, amount=$amount, itemName=$itemName")

            // API 키가 없으면 테스트 모드로 동작
            if (secretKey.isBlank()) {
                logger.warn("토스페이먼츠 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.")
                return PaymentResponse(
                    success = true,
                    paymentUrl = "$returnUrl?method=TOSS&success=true&paymentKey=TEST_${orderId}&orderId=$orderId",
                    paymentKey = "TEST_${orderId}"
                )
            }

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Basic ${Base64.getEncoder().encodeToString("$secretKey:".toByteArray())}")
            }

            val requestBody = mapOf(
                "amount" to amount,
                "orderId" to orderId,
                "orderName" to itemName,
                "customerName" to customerName,
                "customerEmail" to (customerEmail ?: ""),
                "customerMobilePhone" to (customerPhone ?: ""),
                "successUrl" to "$returnUrl?method=TOSS&success=true",
                "failUrl" to "$returnUrl?method=TOSS&success=false"
            )

            val request = HttpEntity(requestBody, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payments",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val paymentUrl = body["checkout"]?.let { checkout ->
                    (checkout as? Map<*, *>)?.get("url") as? String
                } ?: body["url"] as? String
                val paymentKey = body["paymentKey"] as? String

                if (paymentUrl != null) {
                    logger.info("토스페이먼츠 결제 URL 생성 성공: $paymentUrl")
                    return PaymentResponse(
                        success = true,
                        paymentUrl = paymentUrl,
                        paymentKey = paymentKey
                    )
                }
            }

            logger.warn("토스페이먼츠 결제 요청 실패: ${response.statusCode}")
            return PaymentResponse(
                success = false,
                errorMessage = "토스페이먼츠 결제 요청에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("토스페이먼츠 결제 요청 중 오류 발생", e)
            return PaymentResponse(
                success = false,
                errorMessage = "토스페이먼츠 결제 요청 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun approvePayment(
        paymentKey: String,
        orderId: String,
        amount: Int
    ): PaymentApprovalResponse {
        try {
            logger.info("토스페이먼츠 결제 승인: paymentKey=$paymentKey, orderId=$orderId")

            // 테스트 모드 처리
            if (paymentKey.startsWith("TEST_")) {
                logger.info("테스트 모드 결제 승인: paymentKey=$paymentKey")
                return PaymentApprovalResponse(
                    success = true,
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = amount,
                    paidAt = java.time.Instant.now().epochSecond
                )
            }

            // API 키가 없으면 테스트 모드로 동작
            if (secretKey.isBlank()) {
                logger.warn("토스페이먼츠 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.")
                return PaymentApprovalResponse(
                    success = true,
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = amount,
                    paidAt = java.time.Instant.now().epochSecond
                )
            }

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Basic ${Base64.getEncoder().encodeToString("$secretKey:".toByteArray())}")
            }

            val requestBody = mapOf(
                "paymentKey" to paymentKey,
                "orderId" to orderId,
                "amount" to amount
            )

            val request = HttpEntity(requestBody, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payments/confirm",
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

                logger.info("토스페이먼츠 결제 승인 성공: paymentKey=$paymentKey")
                return PaymentApprovalResponse(
                    success = true,
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = approvedAmount,
                    paidAt = approvedAt
                )
            }

            logger.warn("토스페이먼츠 결제 승인 실패: ${response.statusCode}")
            return PaymentApprovalResponse(
                success = false,
                errorMessage = "토스페이먼츠 결제 승인에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("토스페이먼츠 결제 승인 중 오류 발생", e)
            return PaymentApprovalResponse(
                success = false,
                errorMessage = "토스페이먼츠 결제 승인 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ): PaymentCancelResponse {
        try {
            logger.info("토스페이먼츠 결제 취소: paymentKey=$paymentKey")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Basic ${Base64.getEncoder().encodeToString("$secretKey:".toByteArray())}")
            }

            val requestBody = mapOf(
                "cancelReason" to cancelReason
            )

            val request = HttpEntity(requestBody, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payments/$paymentKey/cancel",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val cancelAmount = (body["cancelAmount"] as? Number)?.toInt()
                val canceledAt = (body["canceledAt"] as? String)?.let {
                    Instant.parse(it).epochSecond
                } ?: Instant.now().epochSecond

                logger.info("토스페이먼츠 결제 취소 성공: paymentKey=$paymentKey")
                return PaymentCancelResponse(
                    success = true,
                    cancelAmount = cancelAmount,
                    canceledAt = canceledAt
                )
            }

            logger.warn("토스페이먼츠 결제 취소 실패: ${response.statusCode}")
            return PaymentCancelResponse(
                success = false,
                errorMessage = "토스페이먼츠 결제 취소에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("토스페이먼츠 결제 취소 중 오류 발생", e)
            return PaymentCancelResponse(
                success = false,
                errorMessage = "토스페이먼츠 결제 취소 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun getPaymentStatus(paymentKey: String): PaymentStatusResponse {
        try {
            logger.info("토스페이먼츠 결제 상태 조회: paymentKey=$paymentKey")

            val headers = HttpHeaders().apply {
                set("Authorization", "Basic ${Base64.getEncoder().encodeToString("$secretKey:".toByteArray())}")
            }

            val request = HttpEntity<Void>(headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payments/$paymentKey",
                HttpMethod.GET,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val status = when (body["status"] as? String) {
                    "DONE" -> "COMPLETED"
                    "ABORTED" -> "FAILED"
                    "CANCELED" -> "CANCELLED"
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
            logger.error("토스페이먼츠 결제 상태 조회 중 오류 발생", e)
            return PaymentStatusResponse(
                status = "PENDING",
                paymentKey = paymentKey
            )
        }
    }
}


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
 * 카카오페이 결제 제공자
 * 카카오페이 API 문서: https://developers.kakao.com/docs/latest/ko/kakaopay/overview
 */
@Component
class KakaoPayProvider(
    @Value("\${payment.kakao.admin-key:}")
    private val adminKey: String,
    @Value("\${payment.kakao.cid:}")
    private val cid: String, // 가맹점 코드
    @Value("\${payment.kakao.api-url:https://kapi.kakao.com}")
    private val apiUrl: String,
    @Value("\${payment.return-url:http://localhost:3000/payment/callback}")
    private val returnUrl: String,
    private val restTemplate: RestTemplate
) : PaymentProvider {
    private val logger = LoggerFactory.getLogger(KakaoPayProvider::class.java)

    override val supportedMethod: PaymentMethod = PaymentMethod.KAKAO_PAY

    override fun createPayment(
        orderId: String,
        amount: Int,
        itemName: String,
        customerName: String,
        customerEmail: String?,
        customerPhone: String?
    ): PaymentResponse {
        try {
            logger.info("카카오페이 결제 요청: orderId=$orderId, amount=$amount, itemName=$itemName")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                set("Authorization", "KakaoAK $adminKey")
            }

            // Form URL Encoded 형식으로 변환 (URL 인코딩 필요)
            val formData = LinkedHashMap<String, String>().apply {
                put("cid", cid)
                put("partner_order_id", orderId)
                put("partner_user_id", customerName)
                put("item_name", itemName)
                put("quantity", "1")
                put("total_amount", amount.toString())
                put("tax_free_amount", "0")
                put("approval_url", "$returnUrl?method=KAKAO_PAY&success=true")
                put("cancel_url", "$returnUrl?method=KAKAO_PAY&success=false")
                put("fail_url", "$returnUrl?method=KAKAO_PAY&success=false")
            }

            // MultiValueMap으로 변환 (Spring이 자동으로 Form URL Encoded로 변환)
            val formDataMap = org.springframework.util.LinkedMultiValueMap<String, String>().apply {
                formData.forEach { (key, value) -> add(key, value) }
            }

            val request = HttpEntity(formDataMap, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payment/ready",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val paymentUrl = body["next_redirect_pc_url"] as? String
                val tid = body["tid"] as? String // 카카오페이 거래 ID

                if (paymentUrl != null && tid != null) {
                    logger.info("카카오페이 결제 URL 생성 성공: $paymentUrl")
                    return PaymentResponse(
                        success = true,
                        paymentUrl = paymentUrl,
                        paymentKey = tid // 카카오페이는 tid를 paymentKey로 사용
                    )
                }
            }

            logger.warn("카카오페이 결제 요청 실패: ${response.statusCode}")
            return PaymentResponse(
                success = false,
                errorMessage = "카카오페이 결제 요청에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("카카오페이 결제 요청 중 오류 발생", e)
            return PaymentResponse(
                success = false,
                errorMessage = "카카오페이 결제 요청 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun approvePayment(
        paymentKey: String, // tid
        orderId: String,
        amount: Int
    ): PaymentApprovalResponse {
        try {
            logger.info("카카오페이 결제 승인: paymentKey=$paymentKey, orderId=$orderId")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                set("Authorization", "KakaoAK $adminKey")
            }

            // MultiValueMap으로 변환
            val formDataMap = org.springframework.util.LinkedMultiValueMap<String, String>().apply {
                add("cid", cid)
                add("tid", paymentKey)
                add("partner_order_id", orderId)
                add("partner_user_id", orderId) // 사용자 ID는 orderId로 대체
                add("pg_token", orderId) // 실제로는 콜백에서 받은 pg_token을 사용해야 함
            }

            val request = HttpEntity(formDataMap, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payment/approve",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val approvedAmount = (body["amount"] as? Map<*, *>)?.let {
                    (it["total"] as? Number)?.toInt()
                } ?: amount
                val approvedAt = (body["approved_at"] as? Long) ?: Instant.now().epochSecond

                logger.info("카카오페이 결제 승인 성공: paymentKey=$paymentKey")
                return PaymentApprovalResponse(
                    success = true,
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = approvedAmount,
                    paidAt = approvedAt
                )
            }

            logger.warn("카카오페이 결제 승인 실패: ${response.statusCode}")
            return PaymentApprovalResponse(
                success = false,
                errorMessage = "카카오페이 결제 승인에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("카카오페이 결제 승인 중 오류 발생", e)
            return PaymentApprovalResponse(
                success = false,
                errorMessage = "카카오페이 결제 승인 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun cancelPayment(
        paymentKey: String, // tid
        cancelReason: String
    ): PaymentCancelResponse {
        try {
            logger.info("카카오페이 결제 취소: paymentKey=$paymentKey")

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                set("Authorization", "KakaoAK $adminKey")
            }

            // MultiValueMap으로 변환
            val formDataMap = org.springframework.util.LinkedMultiValueMap<String, String>().apply {
                add("cid", cid)
                add("tid", paymentKey)
                add("cancel_amount", "0") // 전체 취소
                add("cancel_tax_free_amount", "0")
                add("cancel_reason", cancelReason)
            }

            val request = HttpEntity(formDataMap, headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payment/cancel",
                HttpMethod.POST,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val cancelAmount = (body["cancel_amount"] as? Map<*, *>)?.let {
                    (it["total"] as? Number)?.toInt()
                }
                val canceledAt = Instant.now().epochSecond

                logger.info("카카오페이 결제 취소 성공: paymentKey=$paymentKey")
                return PaymentCancelResponse(
                    success = true,
                    cancelAmount = cancelAmount,
                    canceledAt = canceledAt
                )
            }

            logger.warn("카카오페이 결제 취소 실패: ${response.statusCode}")
            return PaymentCancelResponse(
                success = false,
                errorMessage = "카카오페이 결제 취소에 실패했습니다."
            )
        } catch (e: Exception) {
            logger.error("카카오페이 결제 취소 중 오류 발생", e)
            return PaymentCancelResponse(
                success = false,
                errorMessage = "카카오페이 결제 취소 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    override fun getPaymentStatus(paymentKey: String): PaymentStatusResponse {
        try {
            logger.info("카카오페이 결제 상태 조회: paymentKey=$paymentKey")

            val headers = HttpHeaders().apply {
                set("Authorization", "KakaoAK $adminKey")
            }

            val request = HttpEntity<Void>(headers)
            val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
                "$apiUrl/v1/payment/order",
                HttpMethod.GET,
                request,
                Map::class.java
            ) as ResponseEntity<Map<String, Any>>

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val body = response.body!!
                val status = when (body["status"] as? String) {
                    "SUCCESS_PAYMENT" -> "COMPLETED"
                    "CANCEL_PAYMENT" -> "CANCELLED"
                    "FAIL_PAYMENT" -> "FAILED"
                    else -> "PENDING"
                }
                val orderId = body["partner_order_id"] as? String
                val amount = (body["amount"] as? Map<*, *>)?.let {
                    (it["total"] as? Number)?.toInt()
                }
                val paidAt = (body["approved_at"] as? Long)

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
            logger.error("카카오페이 결제 상태 조회 중 오류 발생", e)
            return PaymentStatusResponse(
                status = "PENDING",
                paymentKey = paymentKey
            )
        }
    }
}


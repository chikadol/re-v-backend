package com.jihamdol.jihamdolapi.service

import com.jihamdol.jihamdolapi.dto.PaymentRequest
import com.jihamdol.jihamdolapi.dto.PaymentResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class PaymentService(
    private val tossWebClient: WebClient,
    @Value("\${payment.toss.secret-key}") private val tossSecretKey: String
) {
    // This example demonstrates a simple call to Toss Payments' "payments/confirm" endpoint.
    // For real integration follow Toss API docs and secure secret-key properly.
    fun pay(req: PaymentRequest): PaymentResponse {
        // For demo, we will perform a synchronous/blocking call to the API and map response into PaymentResponse.
        // In production prefer reactive or non-blocking flows and robust error handling.
        return try {
            val resp = tossWebClient.post()
                .uri("/payments/confirm")
                .headers { headers -> headers.setBasicAuth(tossSecretKey, "") }
                .bodyValue(mapOf("orderId" to req.orderId, "amount" to req.amount, "method" to req.method))
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            PaymentResponse(true, "payment confirmed", resp?.get("paymentId")?.toString())
        } catch (ex: Exception) {
            PaymentResponse(false, "failed: \${ex.message}")
        }
    }
}

package com.jihamdol.jihamdolapi.dto

data class PaymentRequest(
    val orderId: String,
    val amount: Long,
    val method: String = "CARD"
)

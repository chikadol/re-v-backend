package com.jihamdol.jihamdolapi.dto

data class PaymentResponse(
    val success: Boolean,
    val message: String,
    val paymentId: String? = null
)

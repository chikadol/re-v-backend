package com.jihamdol.jihamdolapi.controller

import com.jihamdol.jihamdolapi.dto.PaymentRequest
import com.jihamdol.jihamdolapi.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/pay")
    fun pay(@RequestBody req: PaymentRequest) = ResponseEntity.ok(paymentService.pay(req))
}

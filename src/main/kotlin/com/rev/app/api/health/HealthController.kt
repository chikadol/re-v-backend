package com.rev.app.api.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class HealthRes(val status: String = "UP")

@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): HealthRes = HealthRes()
}

package com.rev.app.api.controller

import com.rev.app.api.service.ticket.PerformanceService
import com.rev.app.api.service.ticket.dto.PerformanceCreateRequest
import com.rev.app.api.service.ticket.dto.PerformanceRes
import com.rev.app.domain.ticket.entity.PerformanceStatus
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/performances")
class PerformanceController(
    private val performanceService: PerformanceService
) {
    @GetMapping
    fun getAll(): List<PerformanceRes> {
        return performanceService.getAll()
    }

    @GetMapping("/upcoming")
    fun getUpcoming(): List<PerformanceRes> {
        return performanceService.getUpcoming()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): PerformanceRes {
        return performanceService.getById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: PerformanceCreateRequest): PerformanceRes {
        return performanceService.create(request)
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @RequestParam status: PerformanceStatus
    ): PerformanceRes {
        performanceService.updateStatus(id, status)
        return performanceService.getById(id)
    }
}


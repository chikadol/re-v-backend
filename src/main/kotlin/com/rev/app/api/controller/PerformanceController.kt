package com.rev.app.api.controller

import com.rev.app.api.service.ticket.PerformanceService
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.ticket.dto.PerformanceCreateRequest
import com.rev.app.api.service.ticket.dto.PerformanceRes
import com.rev.app.domain.ticket.entity.PerformanceStatus
import com.rev.app.auth.UserRepository
import com.rev.app.auth.UserRole
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/performances")
class PerformanceController(
    private val performanceService: PerformanceService,
    private val userRepository: UserRepository
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
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @Valid @RequestBody request: PerformanceCreateRequest
    ): PerformanceRes {
        val userId = me?.userId ?: throw IllegalArgumentException("로그인이 필요합니다.")
        ensureIdol(userId)
        return performanceService.create(request)
    }

    @PostMapping("/test-data")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTestData(): Map<String, String> {
        // 테스트용 샘플 공연 데이터 생성
        val testPerformances = listOf(
            PerformanceCreateRequest(
                title = "지하돌A 라이브 공연",
                description = "홍대에서 펼쳐지는 지하돌A의 특별한 라이브 공연입니다.",
                venue = "홍대 라이브홀",
                performanceDateTime = LocalDateTime.now().plusDays(7).withHour(19).withMinute(0),
                price = 30000,
                totalSeats = 100,
                imageUrl = null
            ),
            PerformanceCreateRequest(
                title = "지하돌B 단독 공연",
                description = "지하돌B의 첫 번째 단독 공연입니다.",
                venue = "강남 롤링홀",
                performanceDateTime = LocalDateTime.now().plusDays(14).withHour(20).withMinute(0),
                price = 35000,
                totalSeats = 150,
                imageUrl = null
            ),
            PerformanceCreateRequest(
                title = "지하돌C 팬미팅",
                description = "지하돌C와 함께하는 특별한 팬미팅 시간",
                venue = "잠실 실내체육관",
                performanceDateTime = LocalDateTime.now().plusDays(21).withHour(18).withMinute(30),
                price = 25000,
                totalSeats = 200,
                imageUrl = null
            )
        )

        testPerformances.forEach { performanceService.create(it) }

        return mapOf("message" to "${testPerformances.size}개의 테스트 공연 데이터가 생성되었습니다.")
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @RequestParam status: PerformanceStatus
    ): PerformanceRes {
        performanceService.updateStatus(id, status)
        return performanceService.getById(id)
    }

    private fun ensureIdol(userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        require(user.role == UserRole.IDOL) { "지하아이돌 권한이 필요합니다." }
    }
}


package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.idol.IdolService
import com.rev.app.api.service.idol.dto.IdolCreateRequest
import com.rev.app.api.service.idol.dto.IdolRes
import com.rev.app.auth.UserRepository
import com.rev.app.auth.UserRole
import com.rev.app.api.service.ticket.PerformanceService
import com.rev.app.api.service.ticket.dto.PerformanceRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/idols")
class IdolController(
    private val idolService: IdolService,
    private val userRepository: UserRepository,
    private val performanceService: PerformanceService
) {

    @GetMapping
    fun list(): List<IdolRes> = idolService.list()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): IdolRes = idolService.get(id)

    @GetMapping("/{id}/performances")
    fun getPerformances(@PathVariable id: UUID): List<PerformanceRes> =
        performanceService.getByIdol(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @RequestBody req: IdolCreateRequest
    ): IdolRes {
        val userId = me?.userId ?: throw IllegalArgumentException("로그인이 필요합니다.")
        ensureIdol(userId)
        return idolService.create(req.name, req.description, req.imageUrl)
    }

    @DeleteMapping
    @SecurityRequirement(name = "bearerAuth")
    fun deleteAll(
        @AuthenticationPrincipal me: JwtPrincipal?
    ): Map<String, String> {
        val userId = me?.userId ?: throw IllegalArgumentException("로그인이 필요합니다.")
        ensureIdol(userId)
        idolService.deleteAll()
        return mapOf("message" to "모든 아이돌 데이터가 삭제되었습니다.")
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    fun delete(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable id: UUID
    ): Map<String, String> {
        val userId = me?.userId ?: throw IllegalArgumentException("로그인이 필요합니다.")
        ensureIdol(userId)
        idolService.delete(id)
        return mapOf("message" to "아이돌이 삭제되었습니다.")
    }

    private fun ensureIdol(userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        require(user.role == UserRole.IDOL) { "지하아이돌 권한이 필요합니다." }
    }
}


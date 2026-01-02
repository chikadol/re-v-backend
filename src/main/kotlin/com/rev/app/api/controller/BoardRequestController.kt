package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.BoardRequestService
import com.rev.app.api.service.community.dto.BoardRequestCreateRequest
import com.rev.app.api.service.community.dto.BoardRequestProcessRequest
import com.rev.app.api.service.community.dto.BoardRequestRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/board-requests")
@SecurityRequirement(name = "bearerAuth")
class BoardRequestController(
    private val boardRequestService: BoardRequestService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @Valid @RequestBody request: BoardRequestCreateRequest
    ): BoardRequestRes {
        val requesterId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return boardRequestService.create(requesterId, request)
    }
    
    @GetMapping("/my")
    fun listMyRequests(
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable
    ): Page<BoardRequestRes> {
        val requesterId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return boardRequestService.listMyRequests(requesterId, pageable)
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    fun listPending(pageable: Pageable): Page<BoardRequestRes> {
        return boardRequestService.listPending(pageable)
    }
    
    @PostMapping("/{requestId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    fun process(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable requestId: UUID,
        @Valid @RequestBody processRequest: BoardRequestProcessRequest
    ): BoardRequestRes {
        val adminId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return boardRequestService.process(adminId, requestId, processRequest)
    }
    
    @GetMapping("/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    fun getPendingCount(): Map<String, Long> {
        return mapOf("count" to boardRequestService.getPendingCount())
    }
}


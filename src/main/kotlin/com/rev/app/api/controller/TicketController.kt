package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.ticket.TicketService
import com.rev.app.api.service.ticket.dto.TicketPurchaseRequest
import com.rev.app.api.service.ticket.dto.TicketRes
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketService: TicketService
) {
    private val logger = LoggerFactory.getLogger(TicketController::class.java)
    
    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    fun purchase(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @Valid @RequestBody request: TicketPurchaseRequest
    ): TicketRes {
        logger.info("티켓 구매 요청: performanceId=${request.performanceId}, quantity=${request.quantity}, me=$me")
        logger.info("SecurityContext 인증 정보: ${org.springframework.security.core.context.SecurityContextHolder.getContext().authentication}")
        
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        logger.info("사용자 ID 확인: userId=$userId")
        return ticketService.purchase(userId, request)
    }

    @GetMapping("/my")
    fun getMyTickets(
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable
    ): Page<TicketRes> {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return ticketService.getMyTickets(userId, pageable)
    }

    @GetMapping("/{id}")
    fun getById(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable id: UUID
    ): TicketRes {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return ticketService.getById(id, userId)
    }

    @PostMapping("/{id}/confirm")
    fun confirmTicket(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable id: UUID
    ) {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        ticketService.confirmTicket(id, userId)
    }

    @PostMapping("/{id}/cancel")
    fun cancelTicket(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable id: UUID
    ) {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        ticketService.cancelTicket(id, userId)
    }
}


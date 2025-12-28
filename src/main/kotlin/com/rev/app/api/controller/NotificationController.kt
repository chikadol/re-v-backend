// src/main/kotlin/com/rev/app/api/controller/NotificationController.kt
package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
class NotificationController(
    private val service: NotificationService,
    private val notificationService: NotificationService
) {
    @GetMapping
    fun listMine(
        @AuthenticationPrincipal me: JwtPrincipal,
        pageable: Pageable,
        @RequestParam(name = "type", required = false) type: String? = null
    ): Page<NotificationRes> {
        val uid = requireNotNull(me.userId)
        return service.listMine(uid, pageable, type = type)
    }

    @PostMapping("/{notificationId}/read")
    fun markRead(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable notificationId: UUID
    ): NotificationRes {
        val uid = requireNotNull(me.userId)
        return service.markRead(uid, notificationId)
    }

    @PostMapping("/read-all")
    fun markAllRead(@AuthenticationPrincipal me: JwtPrincipal): Map<String, Any> {
        val uid = requireNotNull(me.userId)
        service.markAllRead(uid)
        return mapOf("ok" to true)
    }

    @GetMapping("/unread-count")
    @SecurityRequirement(name = "bearerAuth", required = false) // 인증 선택적
    fun getUnreadCount(
        @AuthenticationPrincipal me: JwtPrincipal?
    ): Map<String, Long> {
        val uid = me?.userId ?: return mapOf("unreadCount" to 0L)
        val count = notificationService.unreadCount(uid)
        return mapOf("unreadCount" to count)
    }
}

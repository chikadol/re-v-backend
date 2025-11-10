package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @GetMapping
    fun listMine(
        @AuthenticationPrincipal me: JwtPrincipal,
        pageable: Pageable
    ): Page<NotificationRes> =
        notificationService.listMine(requireNotNull(me.userId), pageable)

    @PostMapping("/{id}/read")
    fun markRead(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable id: UUID
    ): NotificationRes =
        notificationService.markRead(requireNotNull(me.userId), id)

    @PostMapping("/read-all")
    fun markAllRead(
        @AuthenticationPrincipal me: JwtPrincipal
    ): Map<String, Any> {
        notificationService.markAllRead(requireNotNull(me.userId))
        val cnt = notificationService.unreadCount(requireNotNull(me.userId))
        return mapOf("unread" to cnt)
    }

    @GetMapping("/unread-count")
    fun unreadCount(@AuthenticationPrincipal me: JwtPrincipal): Map<String, Long> =
        mapOf("unread" to notificationService.unreadCount(requireNotNull(me.userId)))
}

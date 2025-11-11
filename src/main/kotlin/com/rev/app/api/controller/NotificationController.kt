// src/main/kotlin/com/rev/app/api/controller/NotificationController.kt
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
    private val service: NotificationService
) {
    @GetMapping
    fun listMine(
        @AuthenticationPrincipal me: JwtPrincipal,
        pageable: Pageable,
        @RequestParam(name = "type", required = false) type: String? = null
    ): () -> Page<NotificationRes> {
        val uid = requireNotNull(me.userId)
        // 프로덕션에서는 Mockito 매처 사용 금지. 그냥 null 체크로 분기.
        return {
            service.listMine(
                uid, pageable,
                bool = true
            )
        }
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
}

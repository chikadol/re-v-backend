// src/main/kotlin/com/rev/app/api/controller/NotificationController.kt
package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ApiResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.notification.NotificationService
import com.rev.app.api.service.notification.dto.NotificationRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
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
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable,
        @RequestParam(name = "type", required = false) type: String? = null
    ): ResponseEntity<ApiResponse<com.rev.app.api.controller.PageResponse<NotificationRes>>> {
        return try {
            val uid = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
            val page = service.listMine(uid, pageable, type = type)
            ResponseHelper.ok(page)
        } catch (e: IllegalArgumentException) {
            ResponseHelper.unauthorized(e.message ?: "인증이 필요합니다.")
        } catch (e: Exception) {
            ResponseHelper.error("NOTIFICATION_LIST_FAILED", "알림 목록을 불러오는 중 오류가 발생했습니다.")
        }
    }

    @PostMapping("/{notificationId}/read")
    fun markRead(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<NotificationRes>> {
        return try {
            val uid = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
            val notification = service.markRead(uid, notificationId)
            ResponseHelper.ok(notification, "알림을 읽음 처리했습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.unauthorized(e.message ?: "인증이 필요합니다.")
        } catch (e: Exception) {
            ResponseHelper.error("NOTIFICATION_READ_FAILED", "알림 읽기 처리 중 오류가 발생했습니다.")
        }
    }

    @PostMapping("/read-all")
    fun markAllRead(@AuthenticationPrincipal me: JwtPrincipal?): ResponseEntity<ApiResponse<Nothing>> {
        return try {
            val uid = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
            service.markAllRead(uid)
            ResponseHelper.ok<Nothing>("모든 알림을 읽음 처리했습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.unauthorized(e.message ?: "인증이 필요합니다.")
        } catch (e: Exception) {
            ResponseHelper.error("NOTIFICATION_READ_ALL_FAILED", "알림 전체 읽기 처리 중 오류가 발생했습니다.")
        }
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal me: JwtPrincipal?
    ): ResponseEntity<ApiResponse<Map<String, Long>>> {
        return try {
            val uid = me?.userId ?: return ResponseHelper.ok(mapOf("unreadCount" to 0L))
            val count = notificationService.unreadCount(uid)
            ResponseHelper.ok(mapOf("unreadCount" to count))
        } catch (e: Exception) {
            ResponseHelper.error("UNREAD_COUNT_FAILED", "읽지 않은 알림 수 조회 중 오류가 발생했습니다.")
        }
    }
}

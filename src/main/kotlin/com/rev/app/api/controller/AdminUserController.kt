package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ApiResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.user.UserManagementService
import com.rev.app.api.service.user.dto.UserManagementRes
import com.rev.app.api.service.user.dto.UserRoleUpdateRequest
import com.rev.app.auth.UserRole
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val userManagementService: UserManagementService
) {
    @GetMapping
    fun list(
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<com.rev.app.api.controller.PageResponse<UserManagementRes>>> {
        return try {
            val page = userManagementService.list(pageable)
            ResponseHelper.ok(page)
        } catch (e: Exception) {
            ResponseHelper.error("USER_LIST_FAILED", "사용자 목록을 불러오는 중 오류가 발생했습니다.")
        }
    }

    @GetMapping("/{userId}")
    fun get(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<UserManagementRes>> {
        return try {
            val user = userManagementService.get(userId)
            ResponseHelper.ok(user)
        } catch (e: IllegalArgumentException) {
            ResponseHelper.notFound("사용자를 찾을 수 없습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("USER_GET_FAILED", "사용자 정보를 불러오는 중 오류가 발생했습니다.")
        }
    }

    @DeleteMapping("/{userId}")
    fun delete(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<Nothing>> {
        return try {
            userManagementService.delete(userId)
            ResponseHelper.ok<Nothing>("사용자가 삭제되었습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.notFound("사용자를 찾을 수 없습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("USER_DELETE_FAILED", "사용자 삭제 중 오류가 발생했습니다.")
        }
    }

    @PatchMapping("/{userId}/role")
    fun updateRole(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UserRoleUpdateRequest
    ): ResponseEntity<ApiResponse<UserManagementRes>> {
        return try {
            userManagementService.updateRole(userId, request.role)
            val user = userManagementService.get(userId)
            ResponseHelper.ok(user, "사용자 역할이 변경되었습니다.")
        } catch (e: IllegalArgumentException) {
            ResponseHelper.notFound("사용자를 찾을 수 없습니다.")
        } catch (e: Exception) {
            ResponseHelper.error("USER_ROLE_UPDATE_FAILED", "사용자 역할 변경 중 오류가 발생했습니다.")
        }
    }
}


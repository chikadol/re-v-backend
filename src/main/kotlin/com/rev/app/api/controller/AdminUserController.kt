package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.user.UserManagementService
import com.rev.app.api.service.user.dto.UserManagementRes
import com.rev.app.api.service.user.dto.UserRoleUpdateRequest
import com.rev.app.auth.UserRole
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
    ): Page<UserManagementRes> {
        return userManagementService.list(pageable)
    }

    @GetMapping("/{userId}")
    fun get(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable userId: UUID
    ): UserManagementRes {
        return userManagementService.get(userId)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable userId: UUID
    ) {
        userManagementService.delete(userId)
    }

    @PatchMapping("/{userId}/role")
    fun updateRole(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UserRoleUpdateRequest
    ): UserManagementRes {
        userManagementService.updateRole(userId, request.role)
        return userManagementService.get(userId)
    }
}


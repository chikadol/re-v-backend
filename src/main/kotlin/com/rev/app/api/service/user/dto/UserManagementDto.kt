package com.rev.app.api.service.user.dto

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRole
import java.time.Instant
import java.util.UUID

data class UserManagementRes(
    val id: UUID,
    val email: String,
    val username: String,
    val role: UserRole,
    val createdAt: Instant?
)

data class UserRoleUpdateRequest(
    @field:jakarta.validation.constraints.NotNull(message = "역할은 필수 항목입니다.")
    val role: UserRole
)

fun UserEntity.toRes(): UserManagementRes = UserManagementRes(
    id = requireNotNull(id),
    email = email,
    username = username,
    role = role,
    createdAt = null // UserEntity에 createdAt 필드가 없으면 null
)


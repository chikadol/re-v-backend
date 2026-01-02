package com.rev.app.api.service.user

import com.rev.app.api.service.user.dto.UserManagementRes
import com.rev.app.api.service.user.dto.toRes
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.auth.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserManagementService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun list(pageable: Pageable): Page<UserManagementRes> {
        return userRepository.findAll(pageable).map { it.toRes() }
    }

    @Transactional(readOnly = true)
    fun get(userId: UUID): UserManagementRes {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        return user.toRes()
    }

    @Transactional
    fun delete(userId: UUID) {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        }
        userRepository.deleteById(userId)
    }

    @Transactional
    fun updateRole(userId: UUID, role: UserRole) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        user.role = role
        userRepository.save(user)
    }
}


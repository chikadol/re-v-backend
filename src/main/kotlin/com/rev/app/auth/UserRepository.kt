package com.rev.app.auth

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

// User는 UUID PK
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: String): UserEntity?
    fun findByProviderAndProviderId(provider: String, providerId: String): UserEntity?
}




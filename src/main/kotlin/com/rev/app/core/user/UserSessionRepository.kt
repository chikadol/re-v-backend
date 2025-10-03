package com.rev.app.core.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserSessionRepository : JpaRepository<UserSession, Long> {
    fun deleteByUserId(userId: Long): Long
}

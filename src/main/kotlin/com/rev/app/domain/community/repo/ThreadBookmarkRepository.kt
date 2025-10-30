package com.rev.app.domain.community.repo

import ThreadBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {

    // threadId 가 Long이면 아래 두 곳의 타입을 Long으로 바꿔줘
    fun findByUserIdAndThreadId(userId: UUID, threadId: UUID): ThreadBookmarkEntity?

    fun existsByUserIdAndThreadId(userId: UUID, threadId: UUID): Boolean

    @Modifying
    @Transactional
    fun deleteByUserIdAndThreadId(userId: UUID, threadId: UUID): Int
}
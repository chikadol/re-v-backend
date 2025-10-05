package com.rev.app.domain.community.repo

import com.rev.app.domain.community.ThreadBookmark
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ThreadBookmarkRepository : JpaRepository<ThreadBookmark, Long> {
    fun findByUserIdAndThreadId(userId: Long, threadId: Long): ThreadBookmark?
}

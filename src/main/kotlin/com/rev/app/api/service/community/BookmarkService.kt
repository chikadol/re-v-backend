package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val repo: ThreadBookmarkRepository,
    private val threadRepo: ThreadRepository,
    private val userRepo: UserRepository,          // ✅ 추가
) {
    @Transactional
    fun toggle(me: JwtPrincipal, threadId: Long): Boolean {
        val userId = requireNotNull(me.userId)
        val existing = repo.findByThread_IdAndUser_Id(threadId, userId)

        return if (existing == null) {
            val thread = threadRepo.getReferenceById(threadId)
            val userRef = userRepo.getReferenceById(userId)     // ✅ UserEntity 레퍼런스
            repo.save(
                ThreadBookmarkEntity(
                    thread = thread,
                    user = userRef                               // ✅ user로 세팅
                )
            )
            true
        } else {
            repo.delete(existing)
            false
        }
    }
}

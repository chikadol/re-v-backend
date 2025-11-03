package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.UserRepository              // ✅ 추가
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BookmarkService(
    private val threadRepository: ThreadRepository,
    private val bookmarkRepository: ThreadBookmarkRepository,
    private val userRepository: UserRepository      // ✅ 추가 주입
) {
    @Transactional
    fun toggle(me: JwtPrincipal, threadId: Long): Boolean {
        val uid = requireNotNull(me.userId)
        val thread = threadRepository.getReferenceById(threadId)
        val user = userRepository.getReferenceById(uid)   // ✅ UserEntity 프록시
        bookmarkRepository.save(
            ThreadBookmarkEntity(
                thread = thread,
                user = user                                 // ✅ user 로 전달
            )
        )
        return true
    }

    @Transactional
    fun count(threadId: Long): Long = bookmarkRepository.countByThread_Id(threadId)
}

package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BookmarkService(
    private val threadRepository: ThreadRepository,
    private val bookmarkRepository: ThreadBookmarkRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun toggle(me: JwtPrincipal, threadId: UUID): Boolean {
        val uid = requireNotNull(me.userId)
        val already = bookmarkRepository.existsByThread_IdAndUser_Id(threadId, uid)
        if (already) {
            bookmarkRepository.deleteByThread_IdAndUser_Id(threadId, uid)
            return false
        }
        val thread = threadRepository.getReferenceById(threadId)
        val user = userRepository.getReferenceById(uid)
        bookmarkRepository.save(ThreadBookmarkEntity(thread = thread, user = user))
        return true
    }

    fun count(threadId: UUID): Long = bookmarkRepository.countByThread_Id(threadId)
}

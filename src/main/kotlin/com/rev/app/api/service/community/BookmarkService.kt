package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BookmarkService(
    private val threadRepository: ThreadRepository,
    private val bookmarkRepository: ThreadBookmarkRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun toggle(me: JwtPrincipal, threadId: UUID): Boolean {
        val uid = me.userId!!
        val exists = bookmarkRepository.existsByUser_IdAndThread_Id(uid, threadId)
        return if (exists) {
            bookmarkRepository.deleteByUser_IdAndThread_Id(uid, threadId)
            false
        } else {
            val thread = threadRepository.getReferenceById(threadId)
            val user = userRepository.getReferenceById(uid)
            bookmarkRepository.save(ThreadBookmarkEntity(user = user, thread = thread))
            true
        }
    }

    @Transactional(readOnly = true)
    fun count(threadId: UUID): Long =
        bookmarkRepository.countByThread_Id(threadId)
}

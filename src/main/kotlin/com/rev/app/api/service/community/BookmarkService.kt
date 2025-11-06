package com.rev.app.api.service.community

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
    fun toggle(userId: UUID, threadId: UUID): Boolean {
        val exists = bookmarkRepository.existsByThread_IdAndUser_Id(threadId, userId)
        if (exists) {
            bookmarkRepository.deleteByThread_IdAndUser_Id(threadId, userId)
            return false
        }
        val thread = threadRepository.getReferenceById(threadId)
        val user = userRepository.getReferenceById(userId)
        bookmarkRepository.save(ThreadBookmarkEntity(thread = thread, user = user))
        return true
    }

    @Transactional(readOnly = true)
    fun count(threadId: UUID): Long = bookmarkRepository.countByThread_Id(threadId)
}

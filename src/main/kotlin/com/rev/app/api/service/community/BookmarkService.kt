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

    /**
     * 존재하면 삭제(언북마크), 없으면 생성(북마크) -> true = 북마크됨, false = 해제됨
     */
    @Transactional
    fun toggle(me: JwtPrincipal, threadId: UUID): Boolean {
        val userId = requireNotNull(me.userId) { "userId missing" }

        // 존재 확인
        val exists = bookmarkRepository.existsByThread_IdAndUser_Id(threadId, userId)
        if (exists) {
            bookmarkRepository.deleteByThread_IdAndUser_Id(threadId, userId)
            return false
        }

        // 프록시 로딩(엔티티 존재 보장)
        val threadRef = threadRepository.getReferenceById(threadId)
        val userRef = userRepository.getReferenceById(userId)

        // 저장
        bookmarkRepository.save(
            ThreadBookmarkEntity(
                thread = threadRef,
                user = userRef
            )
        )
        return true
    }

    @Transactional
    fun count(threadId: UUID): Long =
        bookmarkRepository.countByThread_Id(threadId)
}

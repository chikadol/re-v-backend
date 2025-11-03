// src/main/kotlin/com/rev/app/api/service/community/ReactionService.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.UserRepository                 // ✅ 추가
import com.rev.app.domain.community.entity.ThreadReaction
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class ReactionService(
    private val threadRepository: ThreadRepository,
    private val threadReactionRepository: ThreadReactionRepository,
    private val userRepository: UserRepository          // ✅ 추가 주입
) {
    @Transactional
    fun toggleThreadReaction(me: JwtPrincipal, threadId: Long, type: ReactionType): Boolean {
        val uid = requireNotNull(me.userId)

        // 있으면 제거하고 false 반환
        val removed = threadReactionRepository.deleteByThread_IdAndUser_IdAndReaction(threadId, uid, type)
        if (removed > 0) return false

        // 없으면 추가하고 true 반환
        val thread = threadRepository.getReferenceById(threadId)
        val user = userRepository.getReferenceById(uid)     // ✅ UserEntity 프록시
        threadReactionRepository.save(
            ThreadReaction(
                thread = thread,
                user = user,                                 // ✅ user 로 전달
                reaction = type
            )
        )
        return true
    }

    @Transactional
    fun countThreadReaction(threadId: Long, type: ReactionType): Long =
        threadReactionRepository.countByThreadAndReaction(threadId, type)
}

package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.domain.community.entity.ThreadReaction
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReactionService(
    private val threadRepo: ThreadRepository,
    private val reactionRepo: ThreadReactionRepository,
    private val userRepo: UserRepository
) {
    @Transactional
    fun toggle(threadId: Long, reaction: ReactionType, me: JwtPrincipal): Long {
        val userId: UUID = requireNotNull(me.userId)
        val userRef = userRepo.getReferenceById(userId)
        val thread = threadRepo.findById(threadId).orElseThrow()

        val existing = reactionRepo.findByThread_IdAndUser_Id(threadId, userId)
        val same = existing.find { it.reaction == reaction }
        if (same != null) {
            reactionRepo.delete(same)
        } else {
            // 다른 리액션은 삭제하고 새로 추가(정책에 따라)
            existing.forEach { reactionRepo.delete(it) }
            reactionRepo.save(ThreadReaction(thread = thread, user = userRef, reaction = reaction))
        }
        return reactionRepo.countByThreadAndReaction(threadId, reaction)
    }
    @Transactional(readOnly = true)
    fun count(threadId: Long, reaction: ReactionType): Long =
        reactionRepo.countByThreadAndReaction(threadId, reaction)
}

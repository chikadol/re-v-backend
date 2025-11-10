package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ToggleReactionRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.model.ThreadReactionEntity
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReactionService(
    private val reactionRepo: ThreadReactionRepository,
    private val threadRepo: ThreadRepository,
    private val userRepo: UserRepository
) {
    private val allowed = setOf("LIKE", "LOVE")

    @Transactional
    fun toggle(userId: UUID, threadId: UUID, type: String): ToggleReactionRes {
        val t = type.uppercase()
        require(t in allowed) { "Invalid reaction type: $type" }

        val existing = reactionRepo.findByThread_IdAndUser_IdAndType(threadId, userId, t)
        val toggled = if (existing != null) {
            reactionRepo.delete(existing); false
        } else {
            val thread = threadRepo.getReferenceById(threadId)
            val user = userRepo.getReferenceById(userId)
            reactionRepo.save(ThreadReactionEntity(thread = thread, user = user, type = t))
            true
        }

        val counts = allowed.associateWith { reactionRepo.countByThread_IdAndType(threadId, it) }
        return ToggleReactionRes(toggled = toggled, counts = counts)
    }
}
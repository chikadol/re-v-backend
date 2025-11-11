package com.rev.app.api.service.community
import com.rev.test.*
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.model.ThreadReactionEntity
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.UUID

class ReactionServiceTest {
    private val reactionRepo = Mockito.mock(ThreadReactionRepository::class.java)
    private val threadRepo = Mockito.mock(ThreadRepository::class.java)
    private val userRepo = Mockito.mock(UserRepository::class.java)
    private val service = ReactionService(reactionRepo, threadRepo, userRepo)
    // --- Mockito matcher helpers (Kotlin null/제네릭 안전) ---
    private fun <T> eqK(v: T): T = org.mockito.ArgumentMatchers.eq(v)
    private fun <T> anyK(clazz: Class<T>): T = org.mockito.ArgumentMatchers.any(clazz)

    @Test
    fun toggle_insert_then_counts() {
        val uid = UUID.randomUUID()
        val tid = UUID.randomUUID()

        org.mockito.Mockito.lenient().doReturn(null)
            .`when`(reactionRepo)
            .findByThread_IdAndUser_IdAndType(tid, uid, "LIKE")

        org.mockito.Mockito.lenient().doReturn(
            com.rev.app.domain.community.entity.ThreadEntity(title = "t", content = "c")
        ).`when`(threadRepo).getReferenceById(eqK(tid))

        org.mockito.Mockito.lenient().doReturn(
            com.rev.app.auth.UserEntity(uid, "e@x.com", "u", "p")
        ).`when`(userRepo).getReferenceById(eqK(uid))

        org.mockito.Mockito.lenient().doReturn(
            com.rev.app.domain.community.model.ThreadReactionEntity(thread = null, user = null, type = "LIKE")
        ).`when`(reactionRepo).save(
            anyK(com.rev.app.domain.community.model.ThreadReactionEntity::class.java)
        )

        org.mockito.Mockito.lenient().doReturn(1L)
            .`when`(reactionRepo).countByThread_IdAndType(tid, "LIKE")
        org.mockito.Mockito.lenient().doReturn(0L)
            .`when`(reactionRepo).countByThread_IdAndType(tid, "LOVE")

        val res = service.toggle(uid, tid, "LIKE")
        assertTrue(res.toggled)
        assertEquals(mapOf("LIKE" to 1L, "LOVE" to 0L), res.counts)
    }
}

package com.rev.app.api.service.community

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.model.ThreadReactionEntity
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.test.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

class ReactionServiceTest {

    private val reactionRepo: ThreadReactionRepository = Mockito.mock(ThreadReactionRepository::class.java)
    private val threadRepo: ThreadRepository = Mockito.mock(ThreadRepository::class.java)
    private val userRepo: UserRepository = Mockito.mock(UserRepository::class.java)
    private val service = ReactionService(reactionRepo, threadRepo, userRepo)

    @Test
    fun toggle_insert_then_counts() {
        val uid = UUID.randomUUID()
        val tid = UUID.randomUUID()

        lenientReturn(null)
            .`when`(reactionRepo).findByThread_IdAndUser_IdAndType(tid, uid, "LIKE")

        lenientReturn(ThreadEntity(title = "t", content = "c"))
            .`when`(threadRepo).getReferenceById(eqK(tid))

        lenientReturn(UserEntity(uid, "e@x.com", "u", "p"))
            .`when`(userRepo).getReferenceById(eqK(uid))

        lenientReturn(ThreadReactionEntity(thread = null, user = null, type = "LIKE"))
            .`when`(reactionRepo).save(anyK(ThreadReactionEntity::class.java))

        lenientReturn(1L).`when`(reactionRepo).countByThread_IdAndType(tid, "LIKE")
        lenientReturn(0L).`when`(reactionRepo).countByThread_IdAndType(tid, "LOVE")

        val res = service.toggle(uid, tid, "LIKE")
        assertTrue(res.toggled)
        assertEquals(mapOf("LIKE" to 1L, "LOVE" to 0L), res.counts)
    }
}

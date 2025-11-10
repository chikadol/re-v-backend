package com.rev.app.api.service.community

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
import junit.framework.TestCase.assertTrue
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ReactionServiceTest {
    private val reactionRepo: ThreadReactionRepository = mock()
    private val threadRepo: ThreadRepository = mock()
    private val userRepo: UserRepository = mock()
    private val service = ReactionService(reactionRepo, threadRepo, userRepo)

    @Test
    fun toggle_insert_then_counts() {
        val uid = UUID.randomUUID()
        val tid = UUID.randomUUID()

        whenever(reactionRepo.findByThread_IdAndUser_IdAndType(tid, uid, "LIKE"))
            .thenReturn(null)
        whenever(threadRepo.getReferenceById(tid)).thenReturn(ThreadEntity(title = "t", content = "c"))
        whenever(userRepo.getReferenceById(uid)).thenReturn(
            UserEntity(id = uid, email = "e", username = "u", password = "p")
        )
        whenever(reactionRepo.countByThread_IdAndType(tid, "LIKE")).thenReturn(1L)
        whenever(reactionRepo.countByThread_IdAndType(tid, "LOVE")).thenReturn(0L)

        val res = service.toggle(uid, tid, "LIKE")
        assertTrue(res.toggled)
        assertEquals(mapOf("LIKE" to 1L, "LOVE" to 0L), res.counts)
    }
}
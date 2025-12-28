package com.rev.app.api.service.community

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.model.ThreadReactionEntity
import com.rev.app.domain.community.repo.ThreadReactionRepository
import com.rev.app.domain.community.repo.ThreadRepository
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

        // 1) 아직 좋아요가 없는 상태
        Mockito.lenient()
            .`when`(reactionRepo.findByThread_IdAndUser_IdAndType(tid, uid, "LIKE"))
            .thenReturn(null)

        // 2) thread / user 레퍼런스
        Mockito.lenient()
            .`when`(threadRepo.getReferenceById(tid))
            .thenReturn(ThreadEntity(title = "t", content = "c"))

        Mockito.lenient()
            .`when`(userRepo.getReferenceById(uid))
            .thenReturn(UserEntity(uid, "e@x.com", "u", "p"))

        // 3) save 는 리턴값을 안 쓰기 때문에 굳이 스텁 안 해도 됨
        Mockito.lenient()
            .`when`(reactionRepo.save(Mockito.any(ThreadReactionEntity::class.java)))
            .thenAnswer { it.arguments[0] }  // 그냥 전달된 객체 그대로 리턴

        // 4) 카운트 결과
        Mockito.lenient()
            .`when`(reactionRepo.countByThread_IdAndType(tid, "LIKE"))
            .thenReturn(1L)

        Mockito.lenient()
            .`when`(reactionRepo.countByThread_IdAndType(tid, "LOVE"))
            .thenReturn(0L)

        // 실행
        val res = service.toggle(uid, tid, "LIKE")

        // 검증
        assertTrue(res.toggled)
        assertEquals(mapOf("LIKE" to 1L, "LOVE" to 0L), res.counts)
    }
}

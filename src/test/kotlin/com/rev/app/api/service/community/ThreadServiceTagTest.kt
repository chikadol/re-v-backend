package com.rev.app.api.service.community

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.UUID

class ThreadServiceTagTest {

    private val threadRepository = Mockito.mock(ThreadRepository::class.java)
    private val boardRepository = Mockito.mock(BoardRepository::class.java)
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val tagRepository = Mockito.mock(TagRepository::class.java)
    private val threadTagRepository = Mockito.mock(ThreadTagRepository::class.java)

    private val service = ThreadService(
        threadRepository, boardRepository, userRepository, tagRepository, threadTagRepository
    )

    @Test
    fun create_with_invalid_tags_rejects() {
        val uid = UUID.randomUUID()
        val bid = UUID.randomUUID()

        Mockito.doReturn(UserEntity(uid, "e@x.com", "u", "p"))
            .`when`(userRepository).getReferenceById(ArgumentMatchers.eq(uid))
        Mockito.doReturn(Board(bid, "b", "b-slug", "d"))
            .`when`(boardRepository).getReferenceById(ArgumentMatchers.eq(bid))

        val badTags = listOf("", " spring  ", "#bad!", "a".repeat(101))
        val req = CreateThreadReq(title = "t", content = "c", tags = badTags)

        assertThrows(IllegalArgumentException::class.java) {
            service.createInBoard(uid, bid, req)
        }
    }
}

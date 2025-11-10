package com.rev.app.api.service

import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.TagRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.community.repo.ThreadTagRepository
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class ThreadServiceTagTest {
    private val threadRepository: ThreadRepository = mock()
    private val boardRepository: BoardRepository = mock()
    private val userRepository: UserRepository = mock()
    private val tagRepository: TagRepository = mock()
    private val threadTagRepository: ThreadTagRepository = mock()

    private val service = ThreadService(
        threadRepository, boardRepository, userRepository, tagRepository, threadTagRepository
    )

    @Test
    fun create_with_invalid_tags_rejects() {
        val req =
            CreateThreadReq(title = "t", content = "c", tags = listOf("this-tag-name-is-way-too-long-to-pass-30-chars"))
        assertThrows<IllegalArgumentException> { service.createInBoard(UUID.randomUUID(), UUID.randomUUID(), req) }
    }
}
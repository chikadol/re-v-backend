package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ThreadMapperTest {

    @Test
    fun roundTrip_ok() {
        val boardId = UUID.randomUUID()
        val authorId = UUID.randomUUID()

        val board = Board(
            id = boardId,
            name = "b",
            slug = "b-slug",
            description = "desc"
        )

        val entity = ThreadEntity(
            title = "title",
            content = "content",
            board = board,
            author = com.rev.app.auth.UserEntity(
                id = authorId,
                email = "e@x.com",
                username = "u",
                password = "p"
            ),
            isPrivate = false,
            categoryId = null,
            parent = null
        ).apply {
            id = UUID.randomUUID()
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }

        // 기본 매퍼는 태그 없이
        val res = entity.toRes()
        assertNotNull(res.id)
        assertEquals(entity.title, res.title)
        assertEquals(boardId, res.boardId)
        assertEquals(authorId, res.authorId)
        assertEquals(emptyList<String>(), res.tags)

        // 태그 포함 매퍼
        val withTags = entity.toResWithTags(listOf("kotlin", "spring"))
        assertEquals(listOf("kotlin", "spring"), withTags.tags)
    }
}

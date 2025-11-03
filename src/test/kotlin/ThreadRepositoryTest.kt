package com.rev.app.repo

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import com.rev.app.support.pg.PostgresTC
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat

@DataJpaTest
@ActiveProfiles("test")
class ThreadRepositoryTest(@Autowired private val boardRepository: BoardRepository) : PostgresTC() {

    @Autowired lateinit var threadRepository: ThreadRepository

    @Test
    fun saveAndFind() {
        val saved = threadRepository.save(
            ThreadEntity(
                title = "t",
                content = "c",
                author = UserEntity(
                    id = UUID.randomUUID(),
                    username = "test",
                    email = "test",
                    password = "tewst"
                ),
                tags = listOf("a", "b").toMutableList(),
                categoryId = UUID.randomUUID(),
                isPrivate = false,
                id = 12,
                parentId = 12,
                board = boardRepository.findById(1).get()
            )
        )
        val found = threadRepository.findById(saved.id!!).get()
        assertThat(found.title).isEqualTo("t")
    }
}

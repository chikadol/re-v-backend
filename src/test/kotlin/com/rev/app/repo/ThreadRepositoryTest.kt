package com.rev.app.repo

import com.rev.app.RevApplication
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = [
        "spring.flyway.enabled=true",
        "spring.flyway.schemas=rev",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=rev"
    ]
)
@Sql(scripts = ["/test/sql/02_seed_minimal.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = ["/test/sql/99_cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ThreadRepositoryTest {

    @Autowired lateinit var threadRepository: ThreadRepository
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var boardRepository: BoardRepository

    @Test
    fun saveAndFind() {
        val user = userRepository.save(UserEntity(
            id = UUID.randomUUID(), email = "u@ex.com", username = "u", password = "p"
        ))
        val board = boardRepository.save(Board(
            id = UUID.randomUUID(), name = "b", slug = "b-${UUID.randomUUID()}",
            description = "d"
        ))
        val saved = threadRepository.save(
            ThreadEntity(
                title = "t", content = "c",
                author = user, board = board
            )
        )
        val found = threadRepository.findById(requireNotNull(saved.id)).orElseThrow()
        assertThat(found.author?.id).isEqualTo(user.id)
        assertThat(found.board?.id).isEqualTo(board.id)
    }
}

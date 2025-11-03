package com.rev.app.repo

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.auth.UserRepository
import com.rev.app.support.pg.PostgresTC
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
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.properties.hibernate.default_schema=rev"
    ]
)@Sql(
    scripts = ["/test/sql/01_fix_users_fk.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ThreadRepositoryTest : PostgresTC() {

    @Autowired lateinit var threadRepository: ThreadRepository
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var boardRepository: BoardRepository

    @Test
    fun saveAndFind() {
        // 1) 부모 엔티티 먼저 INSERT (+ flush)
        val user = userRepository.save(
            UserEntity(username = "u", password = "p", email = "e@example.com")
        )
        val board = boardRepository.save(Board(name="b", slug="b", description="d"))
        userRepository.flush()
        boardRepository.flush()

        val saved = threadRepository.save(
            ThreadEntity(
                title="t", content="c",
                author=user, board=board,
                isPrivate=false, categoryId=UUID.randomUUID(),
                parentId=null, tags=listOf("x","y")
            )
        )

        threadRepository.flush()


        // 3) 검증
        val found = threadRepository.findById(requireNotNull(saved.id)).orElseThrow()
        assertThat(found.author?.id).isEqualTo(user.id)
        assertThat(found.board?.id).isEqualTo(board.id)
    }
}

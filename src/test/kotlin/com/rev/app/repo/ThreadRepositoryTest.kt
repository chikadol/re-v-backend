// src/test/kotlin/com/rev/app/repo/ThreadRepositoryTest.kt
package com.rev.app.repo

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.repo.BoardRepository        // ← 메인 패키지의 리포지토리
import com.rev.app.domain.community.repo.ThreadRepository       // ← 메인 패키지의 리포지토리
import com.rev.app.domain.community.repo.CommentRepository      // ← 메인 패키지의 리포지토리
import com.rev.app.auth.UserRepository                          // ← 메인 패키지의 리포지토리
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
        "spring.flyway.enabled=true",
        "spring.flyway.schemas=rev",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=rev",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
    ]
)
@Sql(
    scripts = ["/test/sql/02_seed_minimal.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = ["/test/sql/99_cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ThreadRepositoryTest : PostgresTC() {

    @Autowired lateinit var threadRepository: ThreadRepository
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var boardRepository: BoardRepository

    private fun uniqueEmail() = "e+" + UUID.randomUUID().toString().substring(0,8) + "@example.com"
    private fun uniqueSlug()  = "b_" + UUID.randomUUID().toString().substring(0,6)

    @Test
    fun saveAndFind() {
        val user = userRepository.save(
            UserEntity(
                id = UUID.randomUUID(),
                username = "u-${UUID.randomUUID()}",
                password = "p",
                email = uniqueEmail()
            )
        )
        val board = boardRepository.save(
            Board(
                name = "b",
                slug = uniqueSlug(),
                description = "desc"
            )
        )
        userRepository.flush()
        boardRepository.flush()

        val saved = threadRepository.save(
            ThreadEntity(
                title = "t",
                content = "c",
                author = user,
                board = board,
                categoryId = UUID.randomUUID(),
                parent = null,
                tags = listOf("x", "y")
            )
        )
        threadRepository.flush()

        val found = threadRepository.findById(requireNotNull(saved.id)).orElseThrow()
        assertThat(found.author?.id).isEqualTo(user.id)
        assertThat(found.board?.id).isEqualTo(board.id)
    }
}

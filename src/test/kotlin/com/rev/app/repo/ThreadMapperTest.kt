package com.rev.app.repo

import com.rev.app.api.service.community.dto.toRes
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = [
        "spring.flyway.enabled=false",                 // ✅ Flyway 끔
        "spring.jpa.hibernate.ddl-auto=update",        // 또는 validate (스키마가 이미 있다면)
        "spring.jpa.properties.hibernate.default_schema=rev",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
    ]
)

class ThreadMapperTest {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var boardRepository: BoardRepository
    @Autowired lateinit var threadRepository: ThreadRepository

    private fun uniqueEmail() = "e+" + UUID.randomUUID().toString().substring(0,8) + "@example.com"
    private fun uniqueSlug()  = "b_" + UUID.randomUUID().toString().substring(0,6)

    @Test
    fun roundTrip_ok() {
        // 1) 부모 엔티티 먼저 저장 & flush (id 확보)
        val user = userRepository.save(
            UserEntity(
                id = UUID.randomUUID(),           // 또는 null + DB DEFAULT
                username = "u-${UUID.randomUUID()}",
                password = "p",
                email = uniqueEmail()
            )
        )
        val board = boardRepository.save(
            Board(
                name = "board",
                slug = uniqueSlug(),
                description = "desc"
            )
        )
        userRepository.flush()
        boardRepository.flush()

        // 2) Thread 저장 & flush (id 확보)
        val thread = threadRepository.save(
            ThreadEntity(
                title = "title",
                content = "content",
                author = user,
                board = board,
                isPrivate = false,
                categoryId = null,
                parent = null,
                tags = listOf("x","y")
            )
        )
        threadRepository.flush()

        // 3) 이제 매핑 호출 → id 가 있으므로 안전
        val dto = thread.toRes()

        // 4) 검증
        assertThat(dto.id).isEqualTo(thread.id)
        assertThat(dto.boardId).isEqualTo(board.id)
        assertThat(dto.authorId).isEqualTo(user.id)
        assertThat(dto.tags).containsExactly("x","y")
    }
}

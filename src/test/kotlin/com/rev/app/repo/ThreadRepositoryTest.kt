package com.rev.app.repo

import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.util.UUID
import org.junit.jupiter.api.Disabled

@Disabled("임시 비활성화 - 테스트 환경/Mockito 정리 후 다시 살릴 예정")
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ThreadRepositoryTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun props(reg: DynamicPropertyRegistry) {
            // ✅ Flyway 비활성화 (이 테스트에선 마이그레이션 사용 안 함)
            reg.add("spring.flyway.enabled") { false }

            // ✅ Hibernate가 테스트 시작 시 스키마 생성, 종료 시 삭제
            reg.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }

            // ✅ 스키마 이름 고정 및 네임스페이스 자동 생성
            reg.add("spring.jpa.properties.hibernate.default_schema") { "rev" }
            reg.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces") { true }
        }
    }

    @Autowired lateinit var threadRepository: ThreadRepository
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var boardRepository: BoardRepository

    @Test
    fun saveAndFind() {
        val user = userRepository.save(
            UserEntity(
                id = UUID.randomUUID(),
                email = "u@ex.com",
                username = "u",
                password = "p"
            )
        )
        val board = boardRepository.save(
            Board(
                id = UUID.randomUUID(),
                name = "b",
                slug = "b-${UUID.randomUUID()}",
                description = "d"
            )
        )
        val saved = threadRepository.save(
            ThreadEntity(
                title = "t",
                content = "c",
                author = user,
                board = board
            )
        )

        val found = threadRepository.findById(requireNotNull(saved.id)).orElseThrow()
        assertThat(found.author?.id).isEqualTo(user.id)
        assertThat(found.board?.id).isEqualTo(board.id)
    }
}

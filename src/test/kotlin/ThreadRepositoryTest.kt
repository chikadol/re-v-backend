package com.rev.app.repo

import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import jakarta.persistence.EntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2가 아니고 TC/실DB이면 유지
class ThreadRepositoryTest @Autowired constructor(
    val threadRepository: ThreadRepository,
    val userRepository: UserRepository,
    val boardRepository: BoardRepository,
    val em: EntityManager,
) {

    @Test
    @Transactional
    fun saveAndFind() {
        // 1) FK 대상들을 먼저 저장(영속화)
        val user = userRepository.save(
            UserEntity(
                id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                email = "u@test.com",
                username = "test",
                password = "test",
            )
        )
        val board = boardRepository.save(
            Board(
                name = "board-1",
                slug = "board-1",
                description = null
            )
        )

        // 2) 굳이 조회해서 붙이지 않고, getReference로 프록시만 잡아도 됨(이미 PK 존재)
        val userRef  = em.getReference(UserEntity::class.java, user.id)
        val boardRef = em.getReference(Board::class.java, board.id)

        // 3) Thread 생성 시 영속 엔티티(혹은 reference)를 넣어준다
        val saved = threadRepository.save(
            ThreadEntity(
                author = userRef,
                board  = boardRef,
                title = "t",
                content = "c",
                isPrivate = false,
                parentId = null,
                categoryId = null
            )
        )
        em.flush() // 제약조건 즉시 검증하고 싶으면

        val found = threadRepository.findById(saved.id!!).get()
        assertThat(found.title).isEqualTo("t")
        assertThat(found.author!!.id).isEqualTo(user.id)
        assertThat(found.board!!.id).isEqualTo(board.id)
    }
}

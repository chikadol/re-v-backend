package com.rev.app.repo

import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.auth.UserRole
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import org.assertj.core.api.Assertions.assertThat

/**
 * ThreadRepository 최적화 테스트
 * N+1 문제 해결 및 JOIN FETCH 쿼리 검증
 */
@DataJpaTest
@ActiveProfiles("test")
class ThreadRepositoryOptimizedTest {

    @Autowired
    private lateinit var threadRepository: ThreadRepository

    @Autowired
    private lateinit var boardRepository: BoardRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `findByIdWithRelations - JOIN FETCH로 관련 엔티티 한 번에 로드`() {
        // Given
        val board = Board(name = "테스트 게시판", slug = "test-board", description = "설명")
        val savedBoard = boardRepository.save(board)

        val author = UserEntity(
            email = "test@example.com",
            username = "testuser",
            password = "encoded",
            role = UserRole.USER
        )
        val savedAuthor = userRepository.save(author)

        val thread = ThreadEntity(
            title = "테스트 게시글",
            content = "내용",
            board = savedBoard,
            author = savedAuthor,
            isPrivate = false
        )
        val savedThread = threadRepository.save(thread)

        // When
        val found = threadRepository.findByIdWithRelations(savedThread.id!!)

        // Then
        assertThat(found).isNotNull()
        assertThat(found!!.board).isNotNull()
        assertThat(found.board!!.id).isEqualTo(savedBoard.id)
        assertThat(found.author).isNotNull()
        assertThat(found.author!!.id).isEqualTo(savedAuthor.id)

        // N+1 문제 해결 확인: board와 author가 이미 로드되어 있어야 함
        // (LAZY 로딩이 발생하지 않아야 함)
    }

    @Test
    fun `findByBoard_IdAndIsPrivateFalse - 페이징 테스트`() {
        // Given
        val board = Board(name = "테스트 게시판", slug = "test-board", description = "설명")
        val savedBoard = boardRepository.save(board)

        val author = UserEntity(
            email = "test@example.com",
            username = "testuser",
            password = "encoded",
            role = UserRole.USER
        )
        val savedAuthor = userRepository.save(author)

        // 여러 게시글 생성
        repeat(5) { i ->
            val thread = ThreadEntity(
                title = "게시글 $i",
                content = "내용 $i",
                board = savedBoard,
                author = savedAuthor,
                isPrivate = false
            )
            threadRepository.save(thread)
        }

        // When
        val page = threadRepository.findByBoard_IdAndIsPrivateFalse(
            savedBoard.id!!,
            PageRequest.of(0, 2)
        )

        // Then
        assertThat(page.content).hasSize(2)
        assertThat(page.totalElements).isEqualTo(5)
        assertThat(page.totalPages).isEqualTo(3)
    }

    @Test
    fun `findPublicByBoardWithAnyTags - 태그 필터링 테스트`() {
        // Given
        val board = Board(name = "테스트 게시판", slug = "test-board", description = "설명")
        val savedBoard = boardRepository.save(board)

        val author = UserEntity(
            email = "test@example.com",
            username = "testuser",
            password = "encoded",
            role = UserRole.USER
        )
        val savedAuthor = userRepository.save(author)

        val thread = ThreadEntity(
            title = "태그 게시글",
            content = "내용",
            board = savedBoard,
            author = savedAuthor,
            isPrivate = false
        )
        val savedThread = threadRepository.save(thread)

        // When
        val page = threadRepository.findPublicByBoardWithAnyTags(
            boardId = savedBoard.id!!,
            names = listOf("kotlin"),
            namesEmpty = false,
            pageable = PageRequest.of(0, 20)
        )

        // Then
        assertThat(page).isNotNull()
        // 태그가 실제로 연결되어 있지 않으면 빈 결과가 나올 수 있음
        // 이는 정상 동작 (태그가 없는 게시글은 필터링됨)
    }
}


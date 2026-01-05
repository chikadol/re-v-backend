package com.rev.app.api.service.community

import com.rev.app.api.controller.dto.CommentCreateRequest
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.notification.NotificationRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*

class CommentServiceTest {

    private lateinit var commentRepository: CommentRepository
    private lateinit var threadRepository: ThreadRepository
    private lateinit var userRepository: UserRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var commentService: CommentService

    @BeforeEach
    fun setUp() {
        commentRepository = mock()
        threadRepository = mock()
        userRepository = mock()
        notificationRepository = mock()
        commentService = CommentService(
            commentRepository,
            threadRepository,
            userRepository,
            notificationRepository
        )
    }

    @Test
    fun `create - 성공`() {
        // Given
        val authorId = UUID.randomUUID()
        val threadId = UUID.randomUUID()
        val request = CommentCreateRequest(
            threadId = threadId,
            content = "댓글 내용",
            parentId = null
        )

        val author = UserEntity(
            id = authorId,
            email = "test@example.com",
            username = "testuser",
            password = "password",
            role = com.rev.app.auth.UserRole.USER
        )

        val board = Board(
            id = UUID.randomUUID(),
            name = "테스트 게시판",
            slug = "test",
            description = "테스트"
        )

        val thread = ThreadEntity(
            id = threadId,
            title = "테스트 게시글",
            content = "내용",
            board = board,
            author = author,
            isPrivate = false
        )

        val savedComment = CommentEntity(
            id = UUID.randomUUID(),
            thread = thread,
            author = author,
            parent = null,
            content = "댓글 내용"
        )

        whenever(userRepository.getReferenceById(authorId)).thenReturn(author)
        whenever(threadRepository.getReferenceById(threadId)).thenReturn(thread)
        whenever(commentRepository.saveAndFlush(any())).thenReturn(savedComment)

        // When
        val result = commentService.create(authorId, request)

        // Then
        assertNotNull(result)
        assertEquals("댓글 내용", result.content)
        verify(commentRepository).saveAndFlush(any())
    }

    @Test
    fun `create - 대댓글 생성 성공`() {
        // Given
        val authorId = UUID.randomUUID()
        val threadId = UUID.randomUUID()
        val parentId = UUID.randomUUID()

        val request = CommentCreateRequest(
            threadId = threadId,
            content = "대댓글 내용",
            parentId = parentId
        )

        val author = UserEntity(
            id = authorId,
            email = "test@example.com",
            username = "testuser",
            password = "password",
            role = com.rev.app.auth.UserRole.USER
        )

        val board = Board(
            id = UUID.randomUUID(),
            name = "테스트 게시판",
            slug = "test",
            description = "테스트"
        )

        val thread = ThreadEntity(
            id = threadId,
            title = "테스트 게시글",
            content = "내용",
            board = board,
            author = author,
            isPrivate = false
        )

        val parentComment = CommentEntity(
            id = parentId,
            thread = thread,
            author = author,
            parent = null,
            content = "부모 댓글"
        )

        val savedComment = CommentEntity(
            id = UUID.randomUUID(),
            thread = thread,
            author = author,
            parent = parentComment,
            content = "대댓글 내용"
        )

        whenever(userRepository.getReferenceById(authorId)).thenReturn(author)
        whenever(threadRepository.getReferenceById(threadId)).thenReturn(thread)
        whenever(commentRepository.getReferenceById(parentId)).thenReturn(parentComment)
        whenever(commentRepository.saveAndFlush(any())).thenReturn(savedComment)

        // When
        val result = commentService.create(authorId, request)

        // Then
        assertNotNull(result)
        assertEquals("대댓글 내용", result.content)
        assertNotNull(result.parent)
        verify(commentRepository).getReferenceById(parentId)
        verify(commentRepository).saveAndFlush(any())
    }

    @Test
    fun `listThreadComments - 성공`() {
        // Given
        val threadId = UUID.randomUUID()
        val authorId = UUID.randomUUID()
        
        val author = UserEntity(
            id = authorId,
            email = "test@example.com",
            username = "testuser",
            password = "password",
            role = com.rev.app.auth.UserRole.USER
        )

        val board = Board(
            id = UUID.randomUUID(),
            name = "테스트 게시판",
            slug = "test",
            description = "테스트"
        )

        val thread = ThreadEntity(
            id = threadId,
            title = "테스트 게시글",
            content = "내용",
            board = board,
            author = author,
            isPrivate = false
        )

        val comment1 = CommentEntity(
            id = UUID.randomUUID(),
            thread = thread,
            author = author,
            parent = null,
            content = "댓글 1"
        )
        val comment2 = CommentEntity(
            id = UUID.randomUUID(),
            thread = thread,
            author = author,
            parent = null,
            content = "댓글 2"
        )

        whenever(commentRepository.findAllByThread_Id(threadId))
            .thenReturn(listOf(comment1, comment2))

        // When
        val result = commentService.listThreadComments(threadId)

        // Then
        assertEquals(2, result.size)
        verify(commentRepository).findAllByThread_Id(threadId)
    }

    @Test
    fun `listMine - 성공`() {
        // Given
        val authorId = UUID.randomUUID()
        val threadId = UUID.randomUUID()
        val pageable: Pageable = PageRequest.of(0, 10)

        val author = UserEntity(
            id = authorId,
            email = "test@example.com",
            username = "testuser",
            password = "password",
            role = com.rev.app.auth.UserRole.USER
        )

        val board = Board(
            id = UUID.randomUUID(),
            name = "테스트 게시판",
            slug = "test",
            description = "테스트"
        )

        val thread = ThreadEntity(
            id = threadId,
            title = "테스트 게시글",
            content = "내용",
            board = board,
            author = author,
            isPrivate = false
        )

        val comment = CommentEntity(
            id = UUID.randomUUID(),
            thread = thread,
            author = author,
            parent = null,
            content = "내 댓글"
        )

        val page: Page<CommentEntity> = PageImpl(listOf(comment), pageable, 1)

        whenever(commentRepository.findAllByAuthor_Id(authorId, pageable))
            .thenReturn(page)

        // When
        val result = commentService.listMine(authorId, pageable)

        // Then
        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
        verify(commentRepository).findAllByAuthor_Id(authorId, pageable)
    }

    @Test
    fun `listMine - 예외 발생 시 빈 페이지 반환`() {
        // Given
        val authorId = UUID.randomUUID()
        val pageable: Pageable = PageRequest.of(0, 10)

        whenever(commentRepository.findAllByAuthor_Id(authorId, pageable))
            .thenThrow(RuntimeException("Database error"))

        // When
        val result = commentService.listMine(authorId, pageable)

        // Then
        assertEquals(0, result.totalElements)
        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `delete - 성공`() {
        // Given
        val commentId = UUID.randomUUID()

        whenever(commentRepository.existsById(commentId)).thenReturn(true)

        // When
        commentService.delete(commentId)

        // Then
        verify(commentRepository).existsById(commentId)
        verify(commentRepository).deleteById(commentId)
    }

    @Test
    fun `delete - 존재하지 않는 댓글 삭제 시 예외 발생`() {
        // Given
        val commentId = UUID.randomUUID()

        whenever(commentRepository.existsById(commentId)).thenReturn(false)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            commentService.delete(commentId)
        }

        assertEquals("댓글을 찾을 수 없습니다: $commentId", exception.message)
        verify(commentRepository).existsById(commentId)
        verify(commentRepository, never()).deleteById(any())
    }
}


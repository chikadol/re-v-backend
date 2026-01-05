package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BoardCreateRequest
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.BoardRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.*

class BoardServiceTest {

    private lateinit var boardRepository: BoardRepository
    private lateinit var boardService: BoardService

    @BeforeEach
    fun setUp() {
        boardRepository = mock()
        boardService = BoardService(boardRepository)
    }

    @Test
    fun `list - 성공`() {
        // Given
        val board1 = Board(
            id = UUID.randomUUID(),
            name = "게시판 1",
            slug = "board1",
            description = "설명 1"
        )
        val board2 = Board(
            id = UUID.randomUUID(),
            name = "게시판 2",
            slug = "board2",
            description = "설명 2"
        )

        whenever(boardRepository.findAll()).thenReturn(listOf(board1, board2))

        // When
        val result = boardService.list()

        // Then
        assertEquals(2, result.size)
        assertEquals("게시판 1", result[0].name)
        assertEquals("게시판 2", result[1].name)
        verify(boardRepository).findAll()
    }

    @Test
    fun `list - 빈 목록`() {
        // Given
        whenever(boardRepository.findAll()).thenReturn(emptyList())

        // When
        val result = boardService.list()

        // Then
        assertTrue(result.isEmpty())
        verify(boardRepository).findAll()
    }

    @Test
    fun `get - 성공`() {
        // Given
        val boardId = UUID.randomUUID()
        val board = Board(
            id = boardId,
            name = "테스트 게시판",
            slug = "test",
            description = "테스트 설명"
        )

        whenever(boardRepository.findById(boardId)).thenReturn(Optional.of(board))

        // When
        val result = boardService.get(boardId)

        // Then
        assertNotNull(result)
        assertEquals("테스트 게시판", result.name)
        assertEquals("test", result.slug)
        assertEquals("테스트 설명", result.description)
        verify(boardRepository).findById(boardId)
    }

    @Test
    fun `get - 존재하지 않는 게시판 조회 시 예외 발생`() {
        // Given
        val boardId = UUID.randomUUID()

        whenever(boardRepository.findById(boardId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<NoSuchElementException> {
            boardService.get(boardId)
        }

        verify(boardRepository).findById(boardId)
    }

    @Test
    fun `create - 성공`() {
        // Given
        val request = BoardCreateRequest(
            name = "새 게시판",
            slug = "new-board",
            description = "새 게시판 설명"
        )

        val savedBoard = Board(
            id = UUID.randomUUID(),
            name = request.name,
            slug = request.slug,
            description = request.description
        )

        whenever(boardRepository.findAll()).thenReturn(emptyList())
        whenever(boardRepository.saveAndFlush(any())).thenReturn(savedBoard)

        // When
        val result = boardService.create(request)

        // Then
        assertNotNull(result)
        assertEquals("새 게시판", result.name)
        assertEquals("new-board", result.slug)
        assertEquals("새 게시판 설명", result.description)
        verify(boardRepository).findAll()
        verify(boardRepository).saveAndFlush(any())
    }

    @Test
    fun `create - slug 중복 시 예외 발생`() {
        // Given
        val request = BoardCreateRequest(
            name = "새 게시판",
            slug = "existing-slug",
            description = "설명"
        )

        val existingBoard = Board(
            id = UUID.randomUUID(),
            name = "기존 게시판",
            slug = "existing-slug",
            description = "기존 설명"
        )

        whenever(boardRepository.findAll()).thenReturn(listOf(existingBoard))

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            boardService.create(request)
        }

        assertEquals("이미 사용 중인 slug입니다: existing-slug", exception.message)
        verify(boardRepository).findAll()
        verify(boardRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `delete - 성공`() {
        // Given
        val boardId = UUID.randomUUID()

        whenever(boardRepository.existsById(boardId)).thenReturn(true)

        // When
        boardService.delete(boardId)

        // Then
        verify(boardRepository).existsById(boardId)
        verify(boardRepository).deleteById(boardId)
    }

    @Test
    fun `delete - 존재하지 않는 게시판 삭제 시 예외 발생`() {
        // Given
        val boardId = UUID.randomUUID()

        whenever(boardRepository.existsById(boardId)).thenReturn(false)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            boardService.delete(boardId)
        }

        assertEquals("게시판을 찾을 수 없습니다: $boardId", exception.message)
        verify(boardRepository).existsById(boardId)
        verify(boardRepository, never()).deleteById(any())
    }
}


package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.repo.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.UUID

@Disabled("임시 비활성화 - Mockito 설정/환경 정리 후 다시 활성화 예정")
class ThreadServiceTagTest {

    private val threadRepository: ThreadRepository =
        Mockito.mock(ThreadRepository::class.java)
    private val boardRepository: BoardRepository =
        Mockito.mock(BoardRepository::class.java)
    private val userRepository: UserRepository =
        Mockito.mock(UserRepository::class.java)
    private val tagRepository: TagRepository =
        Mockito.mock(TagRepository::class.java)
    private val threadTagRepository: ThreadTagRepository =
        Mockito.mock(ThreadTagRepository::class.java)

    // ✅ 새로 추가된 의존성들 (무조건 있어야 함)
    private val commentRepository: CommentRepository =
        Mockito.mock(CommentRepository::class.java)
    private val bookmarkRepository: ThreadBookmarkRepository =
        Mockito.mock(ThreadBookmarkRepository::class.java)
    private val reactionRepository: ThreadReactionRepository =
        Mockito.mock(ThreadReactionRepository::class.java)

    // ✅ 이름 있는 인자(named parameter)로 생성자 호출 → 순서 달라도 OK
    private val service = ThreadService(
        threadRepository = threadRepository,
        boardRepository = boardRepository,
        userRepository = userRepository,
        tagRepository = tagRepository,
        threadTagRepository = threadTagRepository,
        commentRepository = commentRepository,
        bookmarkRepository = bookmarkRepository,
        reactionRepository = reactionRepository,
    )

    @Test
    fun create_with_invalid_tags_rejects() {
        val uid = UUID.randomUUID()
        val bid = UUID.randomUUID()

        val user = UserEntity(
            id = uid,
            email = "u@example.com",
            username = "user",
            password = "pw"
        )
        val board = Board(
            id = bid,
            name = "board",
            slug = "board-slug",
            description = "desc"
        )

        Mockito.`when`(
            userRepository.getReferenceById(ArgumentMatchers.eq(uid))
        ).thenReturn(user)

        Mockito.`when`(
            boardRepository.getReferenceById(ArgumentMatchers.eq(bid))
        ).thenReturn(board)

        // 유효하지 않은 태그를 넣어서 IllegalArgumentException 나오는지 확인
        val req = CreateThreadReq(
            title = "title",
            content = "content",
            tags = listOf("   ", "") // invalid tags 예시
        )

        assertThrows(IllegalArgumentException::class.java) {
            service.createInBoard(uid, bid, req)
        }
    }
}

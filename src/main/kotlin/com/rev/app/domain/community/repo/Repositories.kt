package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.model.TagEntity
import com.rev.app.domain.community.model.ThreadReactionEntity
import com.rev.app.domain.community.model.ThreadTagEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface BoardRepository : JpaRepository<Board, UUID>

interface ThreadRepository : JpaRepository<ThreadEntity, UUID> {

    fun findByBoard_IdAndIsPrivateFalse(
        boardId: UUID,
        pageable: Pageable
    ): Page<ThreadEntity>

    @Query(
        """
        select distinct th from ThreadEntity th
        join fetch th.board b
        join fetch th.author a
        left join fetch ThreadTagEntity tt on tt.thread = th
        left join fetch TagEntity tg on tg = tt.tag
        where b.id = :boardId and th.isPrivate = false
          and (:namesEmpty = true or lower(tg.name) in :names)
        """
    )
    fun findPublicByBoardWithAnyTags(
        @Param("boardId") boardId: UUID,
        @Param("names") names: Collection<String>,
        @Param("namesEmpty") namesEmpty: Boolean,
        pageable: Pageable
    ): Page<ThreadEntity>

    // 태그와 함께 조회 (N+1 문제 해결)
    @Query(
        """
        select th from ThreadEntity th
        join fetch th.board b
        join fetch th.author a
        where b.id = :boardId and th.isPrivate = false
        """
    )
    fun findByBoard_IdAndIsPrivateFalseWithRelations(
        @Param("boardId") boardId: UUID,
        pageable: Pageable
    ): List<ThreadEntity>

    // 검색 기능: 제목, 내용, 댓글 내용으로 검색
    @Query(
        """
        select distinct th from ThreadEntity th
        join th.board b
        left join CommentEntity c on c.thread = th
        where b.id = :boardId 
          and th.isPrivate = false
          and (lower(th.title) like lower(concat('%', :keyword, '%'))
               or lower(th.content) like lower(concat('%', :keyword, '%'))
               or lower(c.content) like lower(concat('%', :keyword, '%')))
        """
    )
    fun findByBoard_IdAndIsPrivateFalseAndTitleOrContentContaining(
        @Param("boardId") boardId: UUID,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<ThreadEntity>

    // ✅ 여기! 내 글 목록용 메서드
    fun findAllByAuthor_Id(
        authorId: UUID,
        pageable: Pageable
    ): Page<ThreadEntity>

    fun countByAuthor_Id(authorId: UUID): Long
    
    // JOIN FETCH로 N+1 문제 해결
    @Query(
        """
        select th from ThreadEntity th
        left join fetch th.board b
        left join fetch th.author a
        left join fetch th.parent p
        where th.id = :threadId
        """
    )
    fun findByIdWithRelations(@Param("threadId") threadId: UUID): ThreadEntity?
}

interface CommentRepository : JpaRepository<CommentEntity, UUID> {
    fun findAllByThread_Id(threadId: UUID): List<CommentEntity>

    // ✅ 추가: 내 댓글 목록용
    fun findAllByAuthor_Id(
        authorId: UUID,
        pageable: Pageable
    ): Page<CommentEntity>

    fun countByThread_Id(threadId: UUID): Long
    fun countByAuthor_Id(authorId: UUID): Long
}

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {

    // 북마크 개수 (기존 Reaction/Bookmark 카운터용)
    fun countByThread_Id(threadId: UUID): Long

    // 유저가 이 글을 북마크했는지 여부 확인용
    fun findByThread_IdAndUser_Id(threadId: UUID, userId: UUID): ThreadBookmarkEntity?

    // 특정 글에 달린 북마크들 조회 (필요하면 사용)
    fun findAllByThread_Id(threadId: UUID): List<ThreadBookmarkEntity>

    // "내 북마크 목록"용: Thread + Board까지 한 번에 패치
    @Query(
        """
        select b 
        from ThreadBookmarkEntity b
        join fetch b.thread t
        join fetch t.board bd
        where b.user.id = :userId
        """
    )
    fun findAllByUser_IdWithThreadAndBoard(
        @Param("userId") userId: UUID,
        pageable: Pageable
    ): Page<ThreadBookmarkEntity>

    fun countByUser_Id(userId: UUID): Long
}



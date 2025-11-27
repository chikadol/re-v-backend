package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.model.TagEntity
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
        join th.board b
        left join ThreadTagEntity tt on tt.thread = th
        left join TagEntity tg on tg = tt.tag
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

    // ✅ 여기! 내 글 목록용 메서드
    fun findAllByAuthor_Id(
        authorId: UUID,
        pageable: Pageable
    ): Page<ThreadEntity>
}

interface CommentRepository : JpaRepository<CommentEntity, UUID> {
    fun findAllByThread_Id(threadId: UUID): List<CommentEntity>

    // ✅ 추가: 내 댓글 목록용
    fun findAllByAuthor_Id(
        authorId: UUID,
        pageable: Pageable
    ): Page<CommentEntity>
}

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {

    fun countByThread_Id(threadId: UUID): Long

    fun findByThread_IdAndUser_Id(
        threadId: UUID,
        userId: UUID
    ): ThreadBookmarkEntity?

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

    fun findAllByThread_Id(threadId: UUID): List<CommentEntity>
}

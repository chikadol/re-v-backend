package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BoardRepository : JpaRepository<Board, UUID>

interface ThreadRepository : JpaRepository<ThreadEntity, UUID> {
    fun findByBoard_IdAndIsPrivateFalse(boardId: UUID, pageable: Pageable): Page<ThreadEntity>
    fun findAllByBoard_IdAndIsPrivateFalseOrderByCreatedAtDesc(boardId: UUID): List<ThreadEntity>
}

interface CommentRepository : JpaRepository<CommentEntity, UUID> {
    fun findAllByThread_Id(threadId: UUID): List<CommentEntity>
}

interface ThreadBookmarkRepository : JpaRepository<ThreadBookmarkEntity, UUID> {
    fun countByThread_Id(threadId: UUID): Long
    fun findByThread_IdAndUser_Id(threadId: UUID, userId: UUID): ThreadBookmarkEntity?
}

package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.CommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommentRepository : JpaRepository<CommentEntity, Long> {

    // 루트 댓글 (parent IS NULL)
    fun findByThread_IdAndParentIsNullOrderByIdAsc(threadId: Long): List<CommentEntity>

    // 대댓글
    fun findByParent_IdOrderByIdAsc(parentId: Long): List<CommentEntity>

    // 쓰레드 내 전체 개수
    @Query("select count(c) from CommentEntity c where c.thread.id = :threadId")
    fun countByThreadId(@Param("threadId") threadId: Long): Long
}

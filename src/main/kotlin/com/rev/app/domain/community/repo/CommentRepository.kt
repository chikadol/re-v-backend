package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.CommentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<CommentEntity, Long> {

    // 상위 댓글
    fun findByThread_IdAndParentIsNullOrderByIdAsc(threadId: Long, pageable: Pageable): List<CommentEntity>

    // 대댓글(부모 기준)
    fun findByParent_IdOrderByIdAsc(parentId: Long): List<CommentEntity>
}

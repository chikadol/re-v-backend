package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.CommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CommentRepository : JpaRepository<CommentEntity, UUID> {
    // 스레드 ID로 댓글 전체 조회
    fun findAllByThread_Id(threadId: UUID): List<CommentEntity>
}

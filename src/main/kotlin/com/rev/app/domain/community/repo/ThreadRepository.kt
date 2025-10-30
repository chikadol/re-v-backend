package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ThreadRepository : JpaRepository<ThreadEntity, Long> {
    // 공개글만 목록
    fun findAllByIsPrivateFalse(pageable: Pageable): Page<ThreadEntity>
}

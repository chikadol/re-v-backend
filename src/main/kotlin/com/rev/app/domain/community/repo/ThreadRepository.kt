// src/main/kotlin/com/rev/app/domain/community/repo/ThreadRepository.kt
package com.rev.app.domain.community.repo

import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadRepository : JpaRepository<ThreadEntity, Long> {

    fun findAllByIsPrivateFalse(pageable: Pageable): Page<ThreadEntity>
    fun findAllByBoard_IdAndIsPrivateFalse(boardId: Long, pageable: Pageable): Page<ThreadEntity>

}

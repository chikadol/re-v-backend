// src/main/kotlin/com/rev/app/api/service/community/BoardService.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.ThreadRes
import com.rev.app.api.service.community.toRes   // ← 이 import가 있어야 함
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardService(
    private val threadRepository: ThreadRepository
) {
    @Transactional(readOnly = true)
    fun listThreadsByBoard(boardId: Long, pageable: Pageable): Page<ThreadRes> =
        threadRepository
            .findAllByBoard_IdAndIsPrivateFalse(boardId, pageable)
            .map { it.toRes() }   // ← toThreadRes() 말고 toRes()
}

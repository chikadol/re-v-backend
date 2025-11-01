// src/main/kotlin/com/rev/app/api/service/community/ThreadService.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.ThreadRes
import com.rev.app.api.service.community.toEntity
import com.rev.app.api.service.community.toRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun listPublic(pageable: Pageable): Page<ThreadRes> =
        threadRepository.findAllByIsPrivateFalse(pageable).map { it.toRes() }

    @Transactional(readOnly = true)
    fun listByBoard(boardId: Long, pageable: Pageable): Page<ThreadRes> =
        threadRepository.findAllByBoard_IdAndIsPrivateFalse(boardId, pageable).map { it.toRes() }

    @Transactional(readOnly = true)
    fun get(id: Long): ThreadRes =
        threadRepository.findById(id).orElseThrow { NoSuchElementException("thread $id not found") }.toRes()

    @Transactional
    fun createInBoard(boardId: Long, me: JwtPrincipal, req: CreateThreadReq): ThreadRes {
        val boardRef = boardRepository.getReferenceById(boardId)
        val authorRef = userRepository.getReferenceById(
            requireNotNull(me.userId) { "Authenticated userId is null" }
        )
        val entity = req.toEntity(author = authorRef, board = boardRef)
        return threadRepository.save(entity).toRes()
    }
}

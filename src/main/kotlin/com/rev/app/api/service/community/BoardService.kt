package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.toEntity
import com.rev.app.api.service.community.toRes
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BoardService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
) {

    @Transactional(readOnly = true)
    fun listThreads(pageable: Pageable): Page<ThreadRes> =
        threadRepository.findAllByIsPrivateFalse(pageable).map { it.toRes() }  // ✅

    @Transactional
    fun createThread(me: JwtPrincipal, req: CreateThreadReq): ThreadRes {
        val authorId: UUID = requireNotNull(me.userId)
        val authorRef: UserEntity = userRepository.getReferenceById(authorId)
        val saved = threadRepository.save(req.toEntity(authorRef))
        return saved.toRes()
    }

    @Transactional(readOnly = true)
    fun get(id: Long): ThreadRes =
        threadRepository.findById(id).orElseThrow().toRes()
}

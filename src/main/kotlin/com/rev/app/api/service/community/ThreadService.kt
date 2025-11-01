package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.toEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun get(id: Long): ThreadEntity =
        threadRepository.findById(id).orElseThrow()

    @Transactional
    fun create(me: JwtPrincipal, req: CreateThreadReq): ThreadEntity {
        val author = userRepository.getReferenceById(requireNotNull(me.userId))
        return threadRepository.save(req.toEntity(author))
    }

    @Transactional(readOnly = true)
    fun listPublic(pageable: Pageable): Page<ThreadEntity> =
        threadRepository.findAllByIsPrivateFalse(pageable)
}

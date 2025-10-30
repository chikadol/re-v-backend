package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.ThreadRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// toEntity / toRes 확장함수 임포트 (ThreadMappers.kt에 정의되어 있어야 함)
import com.rev.app.api.service.community.toEntity
import com.rev.app.api.service.community.toRes

@Service
class BoardService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val em: EntityManager,
) {
    /** 공개 글 목록 조회 */
    @Transactional(readOnly = true)
    fun listThreads(pageable: Pageable): Page<ThreadRes> =
        threadRepository.findAllByIsPrivateFalse(pageable).map { it.toRes() }

    /** 글 생성 */
    @Transactional
    fun createThread(me: JwtPrincipal, req: CreateThreadReq): ThreadRes {
        val authorId = requireNotNull(me.userId) { "Authenticated userId is null" }
        // UserEntity 프록시 참조 (둘 중 하나만 사용, 아래는 userRepository 방식)
        val authorRef: UserEntity = userRepository.getReferenceById(authorId)
        val entity = req.toEntity(authorRef)
        val saved = threadRepository.save(entity)
        return saved.toRes()
    }

    /** 단건 조회 (필요시) */
    @Transactional(readOnly = true)
    fun get(id: Long): ThreadRes =
        threadRepository.findById(id).orElseThrow { NoSuchElementException("thread $id not found") }.toRes()
}

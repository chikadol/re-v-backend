package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.auth.UserEntity
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.api.service.community.dto.ThreadRes   // ✅ ThreadRes import
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function                      // ✅ Page.map에 필요한 Function

@Service
class BoardService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val em: EntityManager,
) {
    /** 공개 글 목록 조회 */
    @Transactional(readOnly = true)
    fun listThreads(pageable: Pageable): Page<ThreadRes> =
        threadRepository
            .findAllByIsPrivateFalse(pageable)
            // ✅ Spring Data Page.map 을 확실히 타도록 java.util.function.Function 사용
            .map(Function { entity -> entity.toRes() })

    /** 글 생성 */
    @Transactional
    fun createThread(me: JwtPrincipal, req: CreateThreadReq): ThreadRes {
        val authorId = requireNotNull(me.userId) { "Authenticated userId is null" }
        // 프록시 참조로 UserEntity 가져오기
        val authorRef: UserEntity = userRepository.getReferenceById(authorId)
        val entity = req.toEntity(authorRef)         // ✅ toEntity 확장함수 필요(아래 참고)
        val saved = threadRepository.save(entity)
        return saved.toRes()
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    fun get(id: Long): ThreadRes =
        threadRepository.findById(id)
            .orElseThrow { NoSuchElementException("thread $id not found") }
            .toRes()
}

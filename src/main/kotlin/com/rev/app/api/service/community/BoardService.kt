package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.toEntity      // ✅ 명시적으로 임포트
import com.rev.app.api.service.community.toThreadRes  // ✅ 모호성 방지용
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function

@Service
class BoardService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
) {
    @Transactional(readOnly = true)
    fun listThreads(boardId: Long, pageable: Pageable): Page<ThreadRes> =
        // 임시: 공개글만. (보드 필터는 나중에 리포지토리 메서드 추가 후 교체)
        threadRepository.findAllByIsPrivateFalse(pageable)
            .map(Function { it.toThreadRes() })   // ✅ toRes 대신 toThreadRes로 확정

    @Transactional
    fun createThread(me: JwtPrincipal, req: CreateThreadReq): ThreadRes {
        val authorRef = userRepository.getReferenceById(requireNotNull(me.userId))
        val saved = threadRepository.save(req.toEntity(authorRef))
        return saved.toThreadRes()               // ✅ 모호성 제거
    }

    @Transactional(readOnly = true)
    fun getBoardHeader(boardId: Long): String = "board:$boardId"
}

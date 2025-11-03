package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.ThreadRes
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    /** 기존 create(…) 이름을 그대로 두어도 되고, 아래처럼 별칭을 만들어 BoardController가 쓰던 이름을 지원해도 됨 */
    @Transactional
    fun createInBoard(me: UUID, boardId: Long, req: CreateThreadReq): ThreadRes {
        val board = boardRepository.getReferenceById(boardId)
        val author = userRepository.getReferenceById(me)
        val entity = req.toEntity(board, author)
        return threadRepository.save(entity).toRes()
    }

    /** public thread 목록 */
    @Transactional(readOnly = true)
    fun listPublic(boardId: Long, pageable: Pageable): Page<ThreadRes> =
        threadRepository.findAllByBoard_IdAndIsPrivateFalse(boardId, pageable).map { it.toRes() }

    /** 단건 조회 (ThreadController에서 씀) */
    @Transactional(readOnly = true)
    fun get(id: Long): ThreadRes =
        threadRepository.findById(id).orElseThrow { NoSuchElementException("thread $id not found") }.toRes()
}

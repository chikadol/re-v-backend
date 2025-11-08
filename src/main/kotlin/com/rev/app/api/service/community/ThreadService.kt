package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.toRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.domain.community.entity.ThreadEntity
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

    /**
     * 공개 스레드 목록 조회
     */
    @Transactional(readOnly = true)
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> {
        val page = threadRepository.findByBoard_IdAndIsPrivateFalse(boardId, pageable)
        return page.map { it.toRes() }
    }

    /**
     * 특정 보드에 새 스레드 생성
     */
    @Transactional
    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val author = userRepository.getReferenceById(userId)
        val board = boardRepository.getReferenceById(boardId)

        val entity = ThreadEntity(
            title = req.title,
            content = req.content,
            board = board,
            author = author,
            parent = null,
            isPrivate = false,
            categoryId = null,
            tags = req.tags
        )

        val saved = threadRepository.save(entity)
        return saved.toRes()
    }
}

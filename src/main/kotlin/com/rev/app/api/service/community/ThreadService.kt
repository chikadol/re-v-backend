package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ThreadService(
    private val threadRepo: ThreadRepository,
    private val boardRepo: BoardRepository
) {
    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes> =
        threadRepo.findAllByBoard_IdAndIsPrivateFalseOrderByCreatedAtDesc(boardId, pageable)
            .map { it.toRes() }

    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val board = boardRepo.getReferenceById(boardId)
        val entity = ThreadEntity(
            title = req.title,
            content = req.content,
            board = board,
            author = UserEntity(
                id = userId,
                email = UUID.randomUUID().toString(),
                username = userId.toString(),
                password = UUID.randomUUID().toString()
            ),
            isPrivate = false
        )
        val saved = threadRepo.save(entity)
        return saved.toRes()
    }
}

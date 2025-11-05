package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ThreadService {

    fun listPublic(boardId: UUID, pageable: Pageable): Page<ThreadRes>
    fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes
}

// 포트(테스트에서 목킹 용이):
interface ThreadQueryPort {
    fun findPublicByBoardIdOrderByCreatedDesc(boardId: UUID): List<ThreadRes>
}
interface ThreadCommandPort {
    fun create(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes
}

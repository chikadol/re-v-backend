// src/main/kotlin/com/rev/app/api/service/community/ThreadService.kt
package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import java.util.UUID

open class ThreadService(
    private val threadQuery: ThreadQueryPort,
    private val threadCmd: ThreadCommandPort
) {
    open fun listPublic(boardId: UUID): List<ThreadRes> {
        // 정렬은 컨트롤러에서 키만 검증하고, 실제 쿼리는 기본 최신순으로 가정
        return threadQuery.findPublicByBoardIdOrderByCreatedDesc(boardId)
    }

    open fun createInBoard(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes {
        val created = threadCmd.create(userId, boardId, req)
        return created
    }
}

// 포트(테스트에서 목킹 용이):
interface ThreadQueryPort {
    fun findPublicByBoardIdOrderByCreatedDesc(boardId: UUID): List<ThreadRes>
}
interface ThreadCommandPort {
    fun create(userId: UUID, boardId: UUID, req: CreateThreadReq): ThreadRes
}

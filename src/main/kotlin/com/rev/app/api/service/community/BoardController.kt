// src/main/kotlin/com/rev/app/api/service/community/BoardController.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.ThreadRes
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService,
    private val threadService: ThreadService
) {
    @PostMapping("/{boardId}/threads")
    fun createThreadInBoard(
        @PathVariable boardId: Long,
        @AuthenticationPrincipal me: JwtPrincipal,   // ← 2번째 인자
        @RequestBody req: CreateThreadReq            // ← 3번째 인자
    ): ThreadRes =
        threadService.createInBoard(boardId, me, req) // ← 서비스 시그니처와 동일한 순서
}

package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads", produces = [MediaType.APPLICATION_JSON_VALUE])
class ThreadController(
    private val service: ThreadService
) {
    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        pageable: Pageable
    ): Page<ThreadRes> {
        pageable.sort.forEach {
            require(it.property in setOf("createdAt", "updatedAt", "title")) { "invalid sort key" }
        }
        return service.listPublic(boardId, pageable)
    }

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @PathVariable boardId: UUID,
        @AuthenticationPrincipal me: JwtPrincipal,
        @RequestBody req: CreateThreadReq
    ): ThreadRes = service.createInBoard(requireNotNull(me.userId), boardId, req)
}

package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    // 허용 정렬 키 목록
    private val allowedSort = setOf("createdAt", "updatedAt", "title")

    @GetMapping("/{boardId}/threads", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listPublic(
        @PathVariable boardId: UUID,  // ✅ UUID
        @PageableDefault(size = 10) pageable: Pageable
    ): Page<ThreadRes> {
        // ✅ 정렬 검증: 허용 키 외엔 400
        pageable.sort.forEach { order ->
            val prop = order.property
            if (!allowedSort.contains(prop)) {
                throw IllegalArgumentException("Unsupported sort key: $prop")
            }
        }
        return threadService.listPublic(boardId, pageable)
    }

    @PostMapping("/boards/{boardId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createInBoard(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable boardId: UUID,  // ✅ UUID
        @RequestBody req: CreateThreadReq
    ): ThreadRes {
        return threadService.createInBoard(me.userId, boardId, req)
    }
}

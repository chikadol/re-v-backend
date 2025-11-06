package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
@Validated
class ThreadController(
    private val threadService: ThreadService
) {
    // 허용 정렬키 (테스트에서 사용하는 createdAt 포함)
    private val ALLOWED_SORT_KEYS = setOf("createdAt", "updatedAt", "title")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        @RequestParam(required = false, defaultValue = "createdAt,desc") sort: String
    ): Page<ThreadRes> {
        val allowed = setOf("createdAt", "updatedAt", "title")
        val sortProp = sort.substringBefore(',') // "createdAt"
        require(sortProp in allowed) { "Invalid sort key: $sortProp" }
        return threadService.listPublic(boardId, pageable)
    }

    @PostMapping("/boards/{boardId}")
    fun createInBoard(
        @PathVariable boardId: UUID,
        @RequestBody req: com.rev.app.api.service.community.dto.CreateThreadReq,
        @AuthenticationPrincipal principal: com.rev.app.api.security.JwtPrincipal
    ): ThreadRes {
        return threadService.createInBoard(principal.userId!!, boardId, req)
    }
}

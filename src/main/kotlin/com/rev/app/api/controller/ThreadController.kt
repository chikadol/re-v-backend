package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    private val allowedSort = setOf("createdAt")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        @RequestParam(name = "tags", required = false) tags: List<String>?,
        pageable: Pageable
    ): Boolean {
        if (pageable.sort.isSorted && pageable.sort.any { it.property !in allowedSort }) {
            throw IllegalArgumentException("Invalid sort key")
        }
        // ✅ 네임드 인자 사용하지 말고 포지셔널로 호출
        return (tags.isNullOrEmpty())
            threadService.listPublic(boardId, pageable, tags) // 3인자 버전
    }
}
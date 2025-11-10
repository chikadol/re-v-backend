package com.rev.app.api.controller

import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
    ): Page<ThreadRes> {
        // 정렬 키 검증: createdAt만 허용
        if (pageable.sort.isSorted && pageable.sort.any { it.property !in allowedSort }) {
            throw IllegalArgumentException("Invalid sort key")
        }
        // 서비스 호출 (포지셔널 인자)
        return threadService.listPublic(boardId, pageable, tags)
    }
}

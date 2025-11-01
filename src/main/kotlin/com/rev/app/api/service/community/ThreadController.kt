package com.rev.app.api.service.community

import com.rev.app.api.service.community.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    /** 전체 공개 스레드 목록 (보드 무관) */
    @GetMapping
    fun listPublic(pageable: Pageable): Page<ThreadRes> =
        threadService.listPublic(pageable)

    /** 스레드 단건 조회 */
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadRes =
        threadService.get(id)
}

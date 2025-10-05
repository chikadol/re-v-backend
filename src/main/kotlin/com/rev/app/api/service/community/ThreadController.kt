package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.*
import com.rev.app.domain.community.ReactionKind
import com.rev.app.api.service.community.CreateThreadReq
import com.rev.app.api.service.community.ReactionReq
import com.rev.app.api.service.community.ThreadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Thread")
@RestController
@RequestMapping("/threads")
class ThreadController(private val svc: ThreadService) {

    @Operation(summary = "스레드 생성 (임시: authorId 파라미터 사용)")
    @PostMapping
    fun create(@RequestParam authorId: Long, @RequestBody req: ThreadCreateReq): ThreadDetailDto =
        threadDetailDto(svc.createThread(authorId, CreateThreadReq(req.boardSlug, req.title, req.content, req.isAnonymous)))

    @Operation(summary = "스레드 상세 조회")
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadDetailDto =
        threadDetailDto(svc.getThread(id))

    @Operation(summary = "북마크 토글")
    @PostMapping("/{id}/bookmarks")
    fun toggleBookmark(@PathVariable id: Long, @RequestParam userId: Long): ToggleResultDto =
        ToggleResultDto(ok = true, state = svc.toggleBookmark(id, userId))

    @Operation(summary = "스레드 리액션")
    @PostMapping("/{id}/reactions")
    fun react(@PathVariable id: Long, @RequestParam userId: Long, @RequestParam kind: ReactionKind): ToggleResultDto {
        svc.reactThread(id, userId, ReactionReq(kind))
        return ToggleResultDto(ok = true)
    }
}

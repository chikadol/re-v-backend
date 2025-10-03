package com.rev.app.api.community

import com.rev.app.api.community.dto.ThreadDetailDto
import com.rev.app.api.community.dto.ToggleResultDto
import com.rev.app.api.community.dto.threadDetailDto
import com.rev.app.security.AuthPrincipal
import com.rev.app.security.CurrentUser
import com.rev.app.domain.community.ReactionKind
import com.rev.app.service.community.CreateThreadReq
import com.rev.app.service.community.ReactionReq
import com.rev.app.service.community.ThreadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Thread")
@RestController
@RequestMapping("/threads")
class ThreadController(private val svc: ThreadService) {

    @Operation(summary = "스레드 생성")
    @PostMapping
    fun create(@CurrentUser me: AuthPrincipal?, @RequestBody req: CreateThreadReq): ThreadDetailDto =
        threadDetailDto(svc.createThread(me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED"), req))

    @Operation(summary = "스레드 상세 조회")
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadDetailDto = threadDetailDto(svc.getThread(id))

    @Operation(summary = "북마크 토글")
    @PostMapping("/{id}/bookmarks")
    fun toggleBookmark(@CurrentUser me: AuthPrincipal?, @PathVariable id: Long): ToggleResultDto =
        ToggleResultDto(ok = true, state = svc.toggleBookmark(id, me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED")))

    @Operation(summary = "스레드 리액션")
    @PostMapping("/{id}/reactions")
    fun react(@CurrentUser me: AuthPrincipal?, @PathVariable id: Long, @RequestParam kind: ReactionKind): ToggleResultDto {
        svc.reactThread(id, me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED"), ReactionReq(kind))
        return ToggleResultDto(ok = true)
    }
}

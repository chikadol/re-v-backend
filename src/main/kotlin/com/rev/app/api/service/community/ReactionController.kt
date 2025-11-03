package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads/{threadId}/reactions")
class ReactionController(
    private val reactionService: ReactionService
) {
    @PostMapping("/{type}")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: Long,
        @PathVariable type: ReactionType
    ): Map<String, Any> {
        val added = reactionService.toggleThreadReaction(me, threadId, type)
        return mapOf("added" to added)
    }

    @GetMapping("/{type}/count")
    fun count(
        @PathVariable threadId: Long,
        @PathVariable type: ReactionType
    ): Map<String, Long> =
        mapOf("count" to reactionService.countThreadReaction(threadId, type))
}

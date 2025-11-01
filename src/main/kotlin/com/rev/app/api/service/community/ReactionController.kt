package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads/{threadId}/reactions")
class ReactionController(
    private val reactionService: ReactionService
) {

    @PostMapping
    fun toggle(
        @PathVariable threadId: Long,
        @RequestParam type: ReactionType,
        @Me me: JwtPrincipal
    ): Map<String, Long> {
        val count = reactionService.toggle(threadId, type, me)
        return mapOf("count" to count)
    }

    @GetMapping("/count")
    fun count(
        @PathVariable threadId: Long,
        @RequestParam type: ReactionType
    ): Map<String, Long> {
        val count = reactionService.count(threadId, type)
        return mapOf("count" to count)
    }
}

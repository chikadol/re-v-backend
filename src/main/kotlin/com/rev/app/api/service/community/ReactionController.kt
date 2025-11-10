package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ReactionService
import com.rev.app.api.service.community.dto.ToggleReactionRes
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads/{threadId}/reactions")
class ReactionController(
    private val reactionService: ReactionService
) {
    @PostMapping("/{type}")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: UUID,
        @PathVariable type: String
    ): ToggleReactionRes {
        val uid = requireNotNull(me.userId)
        return reactionService.toggle(uid, threadId, type)
    }
}

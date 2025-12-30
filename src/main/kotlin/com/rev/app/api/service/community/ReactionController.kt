package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ReactionService
import com.rev.app.api.service.community.dto.ToggleReactionRes
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ReactionController(
    private val reactionService: ReactionService
) {
    @PostMapping("/{threadId}/reactions/{type}")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable(name = "threadId") threadIdString: String,
        @PathVariable type: String
    ): ToggleReactionRes {
        val uid = requireNotNull(me.userId)
        val threadId = try {
            UUID.fromString(threadIdString)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid threadId format: $threadIdString", e)
        }
        return reactionService.toggle(uid, threadId, type)
    }
}

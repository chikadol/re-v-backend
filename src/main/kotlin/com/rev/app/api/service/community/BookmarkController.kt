package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {

    @PostMapping("/{threadId}/toggle")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: UUID
    ): Map<String, Any> {
        val userId = requireNotNull(me.userId)
        val bookmarked = bookmarkService.toggle(userId, threadId)
        return mapOf("bookmarked" to bookmarked)
    }

    @GetMapping("/{threadId}/count")
    fun count(@PathVariable threadId: UUID): Map<String, Any> =
        mapOf("count" to bookmarkService.count(threadId))
}

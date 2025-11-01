package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import org.springframework.web.bind.annotation.*

data class ToggleBookmarkRes(val bookmarked: Boolean)

@RestController
@RequestMapping("/api/threads/{threadId}/bookmark")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {
    @PostMapping
    fun toggle(
        @PathVariable threadId: Long,
        @Me me: JwtPrincipal
    ): ToggleBookmarkRes = ToggleBookmarkRes(bookmarkService.toggle(me, threadId))
}

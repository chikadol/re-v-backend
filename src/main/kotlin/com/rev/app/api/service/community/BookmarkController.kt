package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads/{threadId}/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {
    @PostMapping
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: Long
    ): Map<String, Any> = mapOf("added" to bookmarkService.toggle(me, threadId))

    @GetMapping("/count")
    fun count(@PathVariable threadId: Long): Map<String, Long> =
        mapOf("count" to bookmarkService.count(threadId))
}

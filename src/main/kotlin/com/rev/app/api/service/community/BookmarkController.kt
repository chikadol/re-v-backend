package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {
    @PostMapping("/threads/{threadId}/toggle")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: UUID
    ): Boolean {
        return bookmarkService.toggle(me, threadId) // ✅ 인자 순서: (me, threadId)
    }

    @GetMapping("/threads/{threadId}/count")
    fun count(@PathVariable threadId: UUID): Long {
        return bookmarkService.count(threadId)
    }
}

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
    @PostMapping("/{threadId}/toggle")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: UUID
    ): Map<String, Any> {
        val toggled = bookmarkService.toggle(requireNotNull(me.userId), threadId)
        val count = bookmarkService.count(threadId)
        return mapOf("bookmarked" to toggled, "count" to count)
    }
}

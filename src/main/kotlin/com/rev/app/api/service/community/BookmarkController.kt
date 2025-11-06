package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {
    @PostMapping("/{threadId}/bookmarks/toggle")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: UUID
    ): ResponseEntity<Map<String, Any>> {
        val userId = requireNotNull(me.userId) { "userId required" }
        val bookmarked = bookmarkService.toggle(userId, threadId)
        return ResponseEntity.ok(mapOf("bookmarked" to bookmarked))
    }

    @GetMapping("/{threadId}/bookmarks/count")
    fun count(@PathVariable threadId: UUID): Map<String, Long> =
        mapOf("count" to bookmarkService.count(threadId))
}

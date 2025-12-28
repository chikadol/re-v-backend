package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.BookmarkService
import com.rev.app.api.service.community.dto.BookmarkCountRes
import com.rev.app.api.service.community.dto.BookmarkToggleRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/bookmarks")
@SecurityRequirement(name = "bearerAuth")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {

    @PostMapping("/threads/{threadId}/toggle")
    fun toggle(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable threadId: UUID
    ): BookmarkToggleRes {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return bookmarkService.toggle(userId, threadId)
    }

    // ✅ 특정 글 북마크 개수 조회
    @GetMapping("/threads/{threadId}/count")
    fun count(
        @PathVariable threadId: UUID
    ): BookmarkCountRes {
        return bookmarkService.countThreadBookmarks(threadId)
    }
}

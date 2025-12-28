package com.rev.app.api.controller

import com.rev.app.api.service.community.BookmarkService
import com.rev.app.api.service.community.dto.BookmarkCountRes
import com.rev.app.api.service.community.dto.BookmarkToggleRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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

    // ✅ 북마크 토글 (테스트용: 가짜 유저 ID 사용)
    @PostMapping("/threads/{threadId}/toggle")
    fun toggle(
        @PathVariable threadId: UUID
    ): BookmarkToggleRes {
        // 다른 데서도 쓰던 테스트용 유저 ID
        val fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        return bookmarkService.toggle(fakeUserId, threadId)
    }

    // ✅ 특정 글 북마크 개수 조회
    @GetMapping("/threads/{threadId}/count")
    fun count(
        @PathVariable threadId: UUID
    ): BookmarkCountRes {
        return bookmarkService.countThreadBookmarks(threadId)
    }
}

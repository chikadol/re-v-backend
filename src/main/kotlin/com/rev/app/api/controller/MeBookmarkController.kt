package com.rev.app.api.controller

import com.rev.app.api.service.community.BookmarkService
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/bookmarks")
class MeBookmarkController(
    private val bookmarkService: BookmarkService
) {

    @GetMapping("/threads")
    fun listMyBookmarks(pageable: Pageable): Page<ThreadRes> {
        // 🔧 지금은 인증 없이 테스트 중이라, 고정 유저 ID 사용
        // (Thread 생성/댓글에서 쓰고 있는 fakeUserId랑 동일하게 맞춰줘)
        val fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        return bookmarkService.listMyBookmarks(fakeUserId, pageable)
    }
}

package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ApiResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.BookmarkService
import com.rev.app.api.service.community.dto.MyBookmarkedThreadRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/bookmarks")
@SecurityRequirement(name = "bearerAuth")
class MeBookmarkController(
    private val bookmarkService: BookmarkService
) {

    @GetMapping("/threads")
    fun listMyBookmarks(
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<com.rev.app.api.controller.PageResponse<MyBookmarkedThreadRes>>> {
        return try {
            val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
            val page = bookmarkService.listMyBookmarks(userId, pageable)
            ResponseHelper.ok(page)
        } catch (e: IllegalArgumentException) {
            ResponseHelper.unauthorized(e.message ?: "인증이 필요합니다.")
        } catch (e: Exception) {
            ResponseHelper.error("BOOKMARK_LIST_FAILED", "북마크 목록을 불러오는 중 오류가 발생했습니다.")
        }
    }
}

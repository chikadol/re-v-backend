package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ThreadCreateRequest
import com.rev.app.api.controller.dto.ThreadResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadDetailRes
import com.rev.app.api.service.community.dto.ThreadRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
@SecurityRequirement(name = "bearerAuth")
class ThreadController(
    private val threadService: ThreadService
) {

    private val allowedSort = setOf("createdAt")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        @RequestParam(name = "tags", required = false) tags: List<String>?,
        @RequestParam(name = "search", required = false) search: String?,
        pageable: Pageable
    ): Page<ThreadRes> {
        // 검색어가 있으면 검색 기능 사용
        if (!search.isNullOrBlank()) {
            return threadService.search(boardId, search, pageable)
        }
        
        // 태그 필터가 있으면 태그 필터 사용
        return if (tags.isNullOrEmpty()) {
            threadService.listPublic(boardId, pageable)
        } else {
            threadService.listPublic(boardId, pageable, tags)
        }
    }

    @GetMapping("/{threadId}")
    fun getDetail(
        @PathVariable threadId: UUID,
        @AuthenticationPrincipal me: JwtPrincipal?
    ): ThreadDetailRes {
        val meId = me?.userId
        return threadService.getDetail(threadId, meId)
    }

    @PostMapping("/{boardId}/threads")
    fun createThread(
        @PathVariable boardId: UUID,
        @RequestBody @Valid req: ThreadCreateRequest,
        @AuthenticationPrincipal me: JwtPrincipal?
    ): ThreadResponse {
        val authorId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        
        val thread = threadService.create(
            boardId = boardId,
            authorId = authorId,
            req = req
        )

        // ThreadResponse.from()은 LAZY 로딩 때문에 null을 반환할 수 있으므로 직접 생성
        return ThreadResponse(
            id = thread.id ?: throw IllegalStateException("Thread ID가 생성되지 않았습니다."),
            boardId = boardId,
            authorId = thread.author?.id,
            title = thread.title,
            content = thread.content,
            createdAt = thread.createdAt ?: java.time.Instant.now()
        )
    }

    @DeleteMapping("/{threadId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteThread(
        @AuthenticationPrincipal me: JwtPrincipal?,
        @PathVariable threadId: UUID
    ): Map<String, String> {
        threadService.delete(threadId)
        return mapOf("message" to "게시글이 삭제되었습니다.")
    }
}

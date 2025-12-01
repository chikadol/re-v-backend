package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ThreadCreateRequest
import com.rev.app.api.controller.dto.ThreadResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadDetailRes
import com.rev.app.api.service.community.dto.ThreadRes
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
class ThreadController(
    private val threadService: ThreadService
) {

    private val allowedSort = setOf("createdAt")

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: UUID,
        @RequestParam(name = "tags", required = false) tags: List<String>?,
        pageable: Pageable
    ): Page<ThreadRes> =
        if (tags.isNullOrEmpty()) threadService.listPublic(boardId, pageable)
        else threadService.listPublic(boardId, pageable, tags)

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
        @RequestBody @Valid req: ThreadCreateRequest
    ): ThreadResponse? {
        // 🔧 테스트용 고정 유저 ID
        val fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val thread = threadService.create(
            boardId = boardId,
            authorId = fakeUserId,
            req = req
        )

        return ThreadResponse.from(thread)
    }
}

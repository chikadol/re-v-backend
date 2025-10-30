package com.rev.app.api.service.community

import com.rev.app.api.security.Me
import com.rev.app.api.security.MeDto
import com.rev.app.api.service.community.dto.*
import com.rev.app.api.service.community.dto.ThreadDto
import io.swagger.v3.oas.models.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.rev.app.api.service.community.ReactionType

import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    @PostMapping
    fun createThread(
        @Me me: MeDto,
        @RequestBody req: CreateThreadReq
    ): ResponseEntity<ThreadDto> =
        ResponseEntity.ok(threadService.create(me.userId, req))

    @GetMapping("/{threadId}")
    fun getThread(
        @Me me: MeDto,
        @PathVariable threadId: Long
    ): ResponseEntity<ThreadDto> =
        ResponseEntity.ok(threadService.get(me.userId, threadId))

    @PostMapping("/{threadId}/reactions")
    fun reactThread(
        @Me me: MeDto,
        @PathVariable threadId: Long,
        @RequestBody req: ThreadReactionReq
    ): ResponseEntity<ThreadDto> =
        ResponseEntity.ok(threadService.reactToThread(me.userId, threadId, req.type))
}
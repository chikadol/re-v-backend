package com.rev.app.api.controller

import com.rev.app.api.controller.dto.CommentCreateRequest
import com.rev.app.api.controller.dto.CommentResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.CommentService
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentRequest
import com.rev.app.domain.community.entity.CommentEntity
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/comments")
@SecurityRequirement(name = "bearerAuth")
class CommentController(
    private val commentService: CommentService
) {
/*
    @PostMapping
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal,
        @RequestBody req: CreateCommentRequest
    ): CommentEntity = commentService.create(requireNotNull(me.userId), req)
*/

    @GetMapping("/threads/{threadId}")
    fun list(@PathVariable threadId: UUID): List<CommentRes> =
        commentService.listThreadComments(threadId)

    @PostMapping
    fun createComment(
        @org.springframework.web.bind.annotation.RequestBody @Valid req: CommentCreateRequest
    ): CommentResponse? {

        val fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val saved = commentService.create(fakeUserId, req)

        return CommentResponse.from(saved)
    }

}

package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody req: CreateCommentRequest
    ): CommentRes = commentService.create(principal.userId, req)

    @GetMapping("/threads/{threadId}")
    fun listByThread(@PathVariable threadId: UUID): List<CommentRes> =
        commentService.listThreadComments(threadId)
}

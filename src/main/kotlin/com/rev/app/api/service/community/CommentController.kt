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
        @AuthenticationPrincipal me: JwtPrincipal,
        @RequestBody req: CreateCommentRequest
    ): CommentRes = commentService.create(requireNotNull(me.userId), req)

    @GetMapping("/threads/{threadId}")
    fun list(@PathVariable threadId: UUID): List<CommentRes> =
        commentService.listThreadComments(threadId)
}

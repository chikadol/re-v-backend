package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateCommentReq
import com.rev.app.api.service.community.dto.CommentRes
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads/{threadId}/comments")
class CommentController(
    private val commentService: CommentService
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal me: JwtPrincipal,
        @PathVariable threadId: Long,
        @RequestBody req: CreateCommentReq
    ): CommentRes =
        commentService.create(me, threadId, req.content, req.parentId)

    @GetMapping
    fun list(
        @PathVariable threadId: Long
    ): List<CommentRes> =
        commentService.listThreadComments(threadId)
}

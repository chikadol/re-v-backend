// src/main/kotlin/com/rev/app/api/service/community/CommentController.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentReq
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads/{threadId}/comments")
class CommentController(
    private val commentService: CommentService
) {
    @PostMapping
    fun add(
        @PathVariable threadId: Long,
        @Me me: JwtPrincipal,
        @RequestBody @Valid req: CreateCommentReq
    ): CommentRes = commentService.addComment(threadId, me.userId, req).toRes()

    @GetMapping
    fun list(@PathVariable threadId: Long): List<CommentRes> =
        commentService.listByThread(threadId).map { it.toRes() }
}

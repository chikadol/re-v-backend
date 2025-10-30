package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import com.rev.app.api.service.CommentService
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentReq
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads")
class CommentController(
    private val commentService: CommentService
) {
    @PostMapping("/{threadId}/comments")
    fun addComment(
        @PathVariable threadId: Long,
        @Me me: JwtPrincipal,
        @RequestBody req: CreateCommentReq
    ): CommentRes {
        val dto = commentService.addComment(
            threadId = threadId,
            authorId = me.userId,        // JWT에서 꺼낸 UUID
            content = req.content,
            parentId = req.parentId
        )
        return CommentRes.from(dto)
    }

    @PostMapping("/comments/{commentId}/likes:toggle")
    fun toggleLike(
        @PathVariable commentId: Long,
        @Me me: JwtPrincipal
    ) = commentService.toggleLike(commentId, me.userId)
}

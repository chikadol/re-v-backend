package com.rev.app.api.service.community

import com.rev.app.api.security.Me
import com.rev.app.api.security.MeDto
import com.rev.app.api.service.CommentService
import com.rev.app.api.service.community.dto.CommentDto
import com.rev.app.api.service.community.dto.ToggleResultDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {
    @PostMapping("/{threadId}")
    fun add(
        @Me me: MeDto,
        @PathVariable threadId: Long,
        @RequestParam(required = false) parentId: String?,
        @RequestBody req: CreateCommentReq
    ): ResponseEntity<CommentDto> {
        val parentIdLong = parentId?.toLong()
        val saved = commentService.addComment(
            threadId = threadId,
            authorId = me.userId,
            content = req.content,
            parentId = parentIdLong
        )
        return ResponseEntity.ok(saved)
    }

    @PostMapping("/{commentId}/toggle-like")
    fun toggleLike(
        @Me me: MeDto,
        @PathVariable commentId: Long
    ): ResponseEntity<ToggleResultDto> =
        ResponseEntity.ok(commentService.toggleLike(commentId, me.userId))
}

data class CreateCommentReq(val content: String)

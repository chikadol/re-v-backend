package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentReq
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    ): CommentRes = commentService.addComment(me, threadId, req)

    @GetMapping
    fun listRoot(
        @PathVariable threadId: Long,
        pageable: Pageable
    ): List<CommentRes> = commentService.listRootComments(threadId, pageable)

    @GetMapping("/{parentId}/children")
    fun listChildren(
        @PathVariable parentId: Long
    ): List<CommentRes> = commentService.listChildren(parentId)
}

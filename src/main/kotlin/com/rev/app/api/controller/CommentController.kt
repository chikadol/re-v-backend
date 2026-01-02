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
        @AuthenticationPrincipal me: JwtPrincipal?,
        @RequestBody @Valid req: CommentCreateRequest
    ): CommentResponse {
        val authorId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        
        val saved = commentService.create(authorId, req)

        // 게시물 작성자와 댓글 작성자가 같은지 확인
        val threadAuthorId = saved.thread?.author?.id
        val commentAuthorId = saved.author?.id
        val isAuthor = threadAuthorId != null && 
                       commentAuthorId != null && 
                       threadAuthorId == commentAuthorId

        // CommentResponse.from()은 LAZY 로딩 때문에 null을 반환할 수 있으므로 직접 생성
        return CommentResponse(
            id = saved.id ?: throw IllegalStateException("Comment ID가 생성되지 않았습니다."),
            threadId = req.threadId,
            authorId = authorId,
            parentId = saved.parent?.id,
            content = saved.content,
            createdAt = saved.createdAt ?: java.time.Instant.now(),
            isAuthor = isAuthor
        )
    }

}

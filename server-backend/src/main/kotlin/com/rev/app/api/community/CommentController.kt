package com.rev.app.api.community

import com.rev.app.api.PageCursorResp
import com.rev.app.api.community.dto.CommentDto
import com.rev.app.api.community.dto.ToggleResultDto
import com.rev.app.api.community.dto.commentDto
import com.rev.app.security.AuthPrincipal
import com.rev.app.security.CurrentUser
import com.rev.app.domain.community.CommentReaction
import com.rev.app.domain.community.ReactionKind
import com.rev.app.service.community.CommentReactionReq
import com.rev.app.service.community.CommentService
import com.rev.app.service.community.CreateCommentReq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Comment")
@RestController
class CommentController(private val svc: CommentService) {

    @Operation(summary = "댓글 생성")
    @PostMapping("/threads/{threadId}/comments")
    fun create(
        @CurrentUser me: AuthPrincipal?,
        @PathVariable threadId: Long,
        @RequestParam content: String,
        @RequestParam(required = false) parentId: Long?,
        @RequestParam(required = false, defaultValue = "false") isAnonymous: Boolean
    ): CommentDto = commentDto(svc.createComment(me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED"), CreateCommentReq(threadId, content, parentId, isAnonymous)))

    @Operation(summary = "댓글 목록 (커서 기반)")
    @GetMapping("/threads/{threadId}/comments")
    fun list(
        @PathVariable threadId: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") size: Int
    ): PageCursorResp<CommentDto> {
        val page = svc.pageByThread(threadId, size, cursor)
        return PageCursorResp(page.items.map(::commentDto), page.nextCursor)
    }

    @Operation(summary = "댓글 리액션")
    @PostMapping("/comments/{id}/reactions")
    fun react(@CurrentUser me: AuthPrincipal?, @PathVariable id: Long, @RequestParam kind: ReactionKind): ToggleResultDto {
        svc.reactComment(id, me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED"), CommentReactionReq(kind))
        return ToggleResultDto(ok = true)
    }
}

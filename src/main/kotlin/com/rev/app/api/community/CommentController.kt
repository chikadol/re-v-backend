package com.rev.app.api.community

import com.rev.app.api.PageCursorResp
import com.rev.app.api.community.dto.*
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
        @PathVariable threadId: Long,
        @RequestParam authorId: Long,
        @RequestParam content: String,
        @RequestParam(required = false) parentId: Long?,
        @RequestParam(required = false, defaultValue = "false") isAnonymous: Boolean
    ): CommentDto = commentDto(svc.createComment(authorId, CreateCommentReq(threadId, content, parentId, isAnonymous)))

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
    fun react(@PathVariable id: Long, @RequestParam userId: Long, @RequestParam kind: ReactionKind): ToggleResultDto {
        svc.reactComment(id, userId, CommentReactionReq(kind))
        return ToggleResultDto(ok = true)
    }
}

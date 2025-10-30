// src/main/kotlin/com/rev/app/api/service/community/CommentController.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import com.rev.app.api.service.CommentService
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentReq
import com.rev.app.domain.community.entity.CommentEntity    // ✅ 추가
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
        // ✅ 서비스가 반환하는 타입을 Entity로 받기
        val entity: CommentEntity = commentService.addComment(
            threadId = threadId,
            authorId = me.userId,          // UUID
            content = req.content,
            parentId = req.parentId
        )
        return entity.toRes()              // ✅ 방금 만든 매퍼 사용
    }
}

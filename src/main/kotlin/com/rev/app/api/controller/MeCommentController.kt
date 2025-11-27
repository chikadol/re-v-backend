package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.CommentService
import com.rev.app.api.service.community.dto.MyCommentRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/comments")
class MeCommentController(
    private val commentService: CommentService
) {

    @GetMapping
    fun listMyComments(
        @AuthenticationPrincipal me: JwtPrincipal,
        pageable: Pageable
    ): Page<MyCommentRes> {
        val uid: UUID = requireNotNull(me.userId)
        return commentService.listMine(uid, pageable)
    }
}

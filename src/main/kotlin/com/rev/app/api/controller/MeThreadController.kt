package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.CommentService
import com.rev.app.api.service.community.dto.MyCommentRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/comments")
@SecurityRequirement(name = "bearerAuth")
class MeCommentController(
    private val commentService: CommentService
) {

    private val allowedSort = setOf("createdAt")

    @GetMapping
    fun listMyComments(
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable
    ): Page<MyCommentRes> {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        
        // sort 검증
        pageable.sort.forEach {
            if (it.property !in allowedSort) {
                throw IllegalArgumentException("Invalid sort: ${it.property}")
            }
        }

        return commentService.listMine(userId, pageable)
    }
}



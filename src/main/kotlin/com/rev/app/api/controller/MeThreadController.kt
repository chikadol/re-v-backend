package com.rev.app.api.controller

import com.rev.app.api.service.community.CommentService
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.MyCommentRes
import com.rev.app.api.service.community.dto.ThreadRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    fun listMyComments(pageable: Pageable): Page<MyCommentRes> {
        // 1) sort 검증 (원하면 생략 가능)
        pageable.sort.forEach {
            if (it.property !in allowedSort) {
                throw IllegalArgumentException("Invalid sort: ${it.property}")
            }
        }

        // 2) threads에서 썼던 것과 같은 fake 유저
        val fakeUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        // 3) CommentService에서 "내 댓글 목록" 가져오는 함수 호출
        return commentService.listMine(fakeUserId, pageable)
        // 이름이 listMine / listMyComments / listByAuthor 등일 수도 있으니까
        // 실제 CommentService 함수 이름에 맞게만 바꿔줘!
    }
}



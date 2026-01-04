package com.rev.app.api.controller

import com.rev.app.api.controller.dto.ApiResponse
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/threads")
@SecurityRequirement(name = "bearerAuth")
class MeThreadController(
    private val threadService: ThreadService
) {

    private val allowedSort = setOf("createdAt", "updatedAt")

    @GetMapping
    fun listMyThreads(
        @AuthenticationPrincipal me: JwtPrincipal?,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<com.rev.app.api.controller.PageResponse<ThreadRes>>> {
        return try {
            val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
            
            // sort 검증
            pageable.sort.forEach {
                if (it.property !in allowedSort) {
                    throw IllegalArgumentException("Invalid sort: ${it.property}")
                }
            }

            val page = threadService.listMyThreads(userId, pageable)
            ResponseHelper.ok(page)
        } catch (e: IllegalArgumentException) {
            ResponseHelper.unauthorized(e.message ?: "인증이 필요합니다.")
        } catch (e: Exception) {
            ResponseHelper.error("THREAD_LIST_FAILED", "내 게시글 목록을 불러오는 중 오류가 발생했습니다.")
        }
    }
}

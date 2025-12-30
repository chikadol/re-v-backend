package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    ): Page<ThreadRes> {
        val userId = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        
        // sort 검증
        pageable.sort.forEach {
            if (it.property !in allowedSort) {
                throw IllegalArgumentException("Invalid sort: ${it.property}")
            }
        }

        return threadService.listMyThreads(userId, pageable)
    }
}

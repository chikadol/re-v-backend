package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.ThreadService
import com.rev.app.api.service.community.dto.ThreadRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/threads")
class MeThreadController(
    private val threadService: ThreadService
) {

    @GetMapping
    fun listMyThreads(
        @AuthenticationPrincipal me: JwtPrincipal,
        pageable: Pageable
    ): Page<ThreadRes> {
        val uid: UUID = requireNotNull(me.userId)
        return threadService.listMyThreads(uid, pageable)
    }
}

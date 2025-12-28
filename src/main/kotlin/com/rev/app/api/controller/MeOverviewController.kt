package com.rev.app.api.controller

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.me.MeOverviewService
import com.rev.app.api.service.me.dto.MeOverviewRes
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/me/overview")
@SecurityRequirement(name = "bearerAuth")
class MeOverviewController(
    private val meOverviewService: MeOverviewService
) {

    @GetMapping
    fun getOverview(
        @AuthenticationPrincipal me: JwtPrincipal?
    ): MeOverviewRes {
        val uid = me?.userId ?: throw IllegalArgumentException("인증이 필요합니다.")
        return meOverviewService.getOverview(uid)
    }
}

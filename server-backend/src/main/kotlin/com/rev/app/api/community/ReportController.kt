package com.rev.app.api.community

import com.rev.app.api.community.dto.ToggleResultDto
import com.rev.app.security.AuthPrincipal
import com.rev.app.security.CurrentUser
import com.rev.app.service.community.ReportReq
import com.rev.app.service.community.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Report")
@RestController
@RequestMapping("/reports")
class ReportController(private val svc: ReportService) {

    @Operation(summary = "스레드 신고")
    @PostMapping("/threads/{id}")
    fun reportThread(@CurrentUser me: AuthPrincipal?, @PathVariable id: Long, @RequestParam(required=false) reason: String?, @RequestParam(required=false) detail: String?): ToggleResultDto {
        svc.reportThread(id, me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED"), ReportReq(reason, detail))
        return ToggleResultDto(ok = true)
    }

    @Operation(summary = "댓글 신고")
    @PostMapping("/comments/{id}")
    fun reportComment(@CurrentUser me: AuthPrincipal?, @PathVariable id: Long, @RequestParam(required=false) reason: String?, @RequestParam(required=false) detail: String?): ToggleResultDto {
        svc.reportComment(id, me?.userId ?: throw IllegalArgumentException("UNAUTHORIZED"), ReportReq(reason, detail))
        return ToggleResultDto(ok = true)
    }
}

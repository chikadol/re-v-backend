package com.rev.app.api.service.community

import com.rev.app.api.security.Me
import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.dto.UpdateThreadReq
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    @PostMapping
    fun create(@Me me: JwtPrincipal, @RequestBody @Valid req: CreateThreadReq): ThreadRes =
        threadService.create(me.userId, req).toRes()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadRes =
        threadService.get(id).toRes()

    @GetMapping
    fun listPublic(@PageableDefault(size = 20) pageable: Pageable) =
        threadService.listPublic(pageable).map { it.toRes() }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Me me: JwtPrincipal,
        @RequestBody req: UpdateThreadReq
    ): ThreadRes = threadService.update(id, me.userId, req).toRes()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @Me me: JwtPrincipal) {
        threadService.delete(id, me.userId)
    }
}

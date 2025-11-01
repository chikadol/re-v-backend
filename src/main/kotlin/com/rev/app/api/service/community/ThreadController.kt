// src/main/kotlin/com/rev/app/api/service/community/ThreadController.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.api.service.community.toRes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.function.Function

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService,
) {
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadRes =
        threadService.get(id).toRes()

    @PostMapping
    fun create(
        @Me me: JwtPrincipal,
        @RequestBody req: CreateThreadReq
    ): ThreadRes =
        threadService.create(me, req).toRes()

    @GetMapping
    fun listPublic(pageable: Pageable): Page<ThreadRes> =
        threadService.listPublic(pageable).map(Function { it.toRes() })
}

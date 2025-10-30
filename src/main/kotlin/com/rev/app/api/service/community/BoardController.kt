package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.security.Me
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads")
class BoardController(
    private val boardService: BoardService
) {
    @GetMapping
    fun list(pageable: Pageable): Page<ThreadRes> =
        boardService.listThreads(pageable)   // ✅ listThreads 호출

    @PostMapping
    fun create(
        @Me me: JwtPrincipal,
        @RequestBody req: CreateThreadReq
    ): ThreadRes =
        boardService.createThread(me, req)   // ✅ createThread 호출

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadRes =
        boardService.get(id)
}

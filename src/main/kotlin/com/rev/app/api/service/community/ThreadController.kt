package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CreateThreadReq
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads")
class ThreadControllerImpl(
    private val threadService: ThreadService
) {

    private val allowedSort = setOf("createdAt", "updatedAt", "title", "id")

    private fun validateSort(pageable: Pageable) {
        pageable.sort.forEach { order ->
            require(order.property in allowedSort) { "Invalid sort: ${order.property}" }
        }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadRes =
        threadService.get(id)

    @PostMapping("/boards/{boardId}")
    @ResponseStatus(HttpStatus.OK) // 필요하면 CREATED로 변경
    fun createInBoard(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable boardId: Long,
        @Valid @RequestBody req: CreateThreadReq
    ): ThreadRes {
        val me = requireNotNull(principal.userId) { "No user id in principal" }
        return threadService.createInBoard(me, boardId, req)
    }

    @GetMapping("/{boardId}/threads")
    fun listPublic(
        @PathVariable boardId: Long,
        pageable: Pageable
    ): Page<ThreadRes> {
        validateSort(pageable)
        return threadService.listPublic(boardId, pageable)
    }
}

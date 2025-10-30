// com.rev.app.api.service.community.ThreadController.kt
package com.rev.app.api.service.community

import com.rev.app.api.security.Me
import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads")
class ThreadController(
    private val threadService: ThreadService
) {
    @PostMapping
    fun create(
        @RequestBody req: CreateThreadReq,
        @Me me: UserEntity
    ): ThreadRes {
        // DTO -> Entity 매핑 (author는 UserEntity로 설정)
        val entity = ThreadEntity(
            title = req.title,
            content = req.content,
            author = me,
            tags = req.tags.toMutableList(),
            categoryId = req.categoryId,
            parentThreadId = req.parentId,
            isPrivate = req.isPrivate
        )
        val saved = threadService.create(entity)
        return ThreadRes.from(saved)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ThreadRes =
        ThreadRes.from(threadService.get(id))

    // 🔧 (중요) reactToThread 관련 호출이 있었다면 일단 주석 처리하거나,
    // 서비스에 구현이 준비될 때까지 엔드포인트를 잠깐 제거하세요.
}

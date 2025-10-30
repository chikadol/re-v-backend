// BoardService.kt
package com.rev.app.api.service.community

import com.rev.app.api.PageCursorResp
import com.rev.app.api.service.community.dto.ThreadDto
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BoardService {
    fun listThreads(boardKey: String, cursor: Long?, size: Int): PageCursorResp<ThreadDto> {
        // 임시: 더미 데이터
        val sample = ThreadDto(
            id = 1L, title = "sample", content = "sample",
            authorId = 1L, createdAt = Instant.now()
        )
        return PageCursorResp(items = listOf(sample), nextCursor = null)
    }
}

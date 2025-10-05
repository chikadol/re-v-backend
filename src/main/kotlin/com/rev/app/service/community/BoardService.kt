package com.rev.app.service.community

import com.rev.app.api.PageCursorResp
import com.rev.app.domain.community.Thread
import com.rev.app.domain.community.repo.BoardRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.util.CursorUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class BoardService(
    private val boards: BoardRepository,
    private val threads: ThreadRepository
) {
    fun getBoardBySlug(slug: String) = boards.findBySlug(slug) ?: throw IllegalArgumentException("BOARD_NOT_FOUND")

    fun listThreadsBySlug(slug: String, size: Int, cursor: String?): PageCursorResp<Thread> {
        val board = getBoardBySlug(slug)
        val c = cursor?.let { CursorUtil.decode(it) }
        val items = threads.pageByBoardKeyset(board.id!!, c?.createdAt, c?.id, PageRequest.of(0, size))
        val next = items.lastOrNull()?.let { CursorUtil.encode(it.createdAt, it.id!!) }
        return PageCursorResp(items, next)
    }
}

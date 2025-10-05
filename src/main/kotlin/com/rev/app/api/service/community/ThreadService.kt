package com.rev.app.service.community

import com.rev.app.api.PageCursorResp
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.ReactionKind
import com.rev.app.domain.community.ThreadBookmark
import com.rev.app.domain.community.ThreadReaction
import com.rev.app.domain.community.repo.*
import com.rev.app.util.CursorUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

// 🔶 java.lang.Thread 와 충돌 피하려고 alias 사용
import com.rev.app.domain.community.Thread as CommunityThread

data class CreateThreadReq(
    val boardSlug: String,
    val title: String,
    val content: String?,
    val isAnonymous: Boolean = false
)

data class ReactionReq(val kind: ReactionKind)

@Service
class ThreadService(
    private val boards: BoardRepository,
    private val threads: ThreadRepository,
    private val threadReactions: ThreadReactionRepository,
    private val bookmarks: ThreadBookmarkRepository
) {
    private fun getBoardBySlug(slug: String): Board =
        boards.findBySlug(slug) ?: throw IllegalArgumentException("BOARD_NOT_FOUND")

    private fun nextDisplayNo(boardId: Long): Long =
        (threads.findMaxDisplayNo(boardId) ?: 0L) + 1

    @Transactional
    fun createThread(authorId: Long, req: CreateThreadReq): CommunityThread {
        val board = getBoardBySlug(req.boardSlug)
        val thread = CommunityThread(
            boardId = board.id!!,
            authorId = authorId,
            title = req.title,
            content = req.content,
            isAnonymous = req.isAnonymous,
            displayNo = nextDisplayNo(board.id!!),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        return threads.save(thread)
    }

    fun getThread(id: Long): CommunityThread =
        threads.findById(id).orElseThrow { IllegalArgumentException("THREAD_NOT_FOUND") }

    fun pageThreadsByBoard(slug: String, size: Int, cursor: String?): PageCursorResp<CommunityThread> {
        val board = getBoardBySlug(slug)
        val c = cursor?.let { CursorUtil.decode(it) }
        val items = threads.pageByBoardKeyset(board.id!!, c?.createdAt, c?.id, PageRequest.of(0, size))
        val next = items.lastOrNull()?.let { CursorUtil.encode(it.createdAt, it.id!!) }
        return PageCursorResp(items, next)
    }

    @Transactional
    fun toggleBookmark(threadId: Long, userId: Long): Boolean {
        val existing = bookmarks.findByUserIdAndThreadId(userId, threadId)
        return if (existing != null) {
            bookmarks.delete(existing)
            false
        } else {
            bookmarks.save(ThreadBookmark(threadId = threadId, userId = userId))
            true
        }
    }

    @Transactional
    fun reactThread(threadId: Long, userId: Long, req: ReactionReq): ThreadReaction {
        val existing = threadReactions.findByUserIdAndThreadId(userId, threadId)
        existing.forEach { threadReactions.delete(it) }
        return threadReactions.save(
            ThreadReaction(threadId = threadId, userId = userId, kind = req.kind)
        )
    }
}

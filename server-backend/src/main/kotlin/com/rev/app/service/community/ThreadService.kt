package com.rev.app.service.community

import com.rev.app.domain.community.*
import com.rev.app.domain.community.repo.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class CreateThreadReq(
    val boardSlug: String,
    val title: String,
    val content: String? = null,
    val isAnonymous: Boolean = false
)
data class ReactionReq(val kind: ReactionKind)

@Service
class ThreadService(
    private val boards: BoardRepository,
    private val threads: ThreadRepository,
    private val bookmarks: BookmarkRepository,
    private val threadReactions: ThreadReactionRepository
) {
    private fun nextDisplayNo(boardId: Long): Long = (threads.findMaxDisplayNo(boardId) ?: 0L) + 1

    @Transactional
    fun createThread(authorId: Long, req: CreateThreadReq): Thread {
        val board = boards.findBySlug(req.boardSlug) ?: throw IllegalArgumentException("BOARD_NOT_FOUND")
        val t = Thread(
            boardId = board.id!!,
            authorId = authorId,
            title = req.title,
            content = req.content,
            isAnonymous = req.isAnonymous,
            displayNo = nextDisplayNo(board.id!!),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        return threads.save(t)
    }

    fun getThread(id: Long): Thread = threads.findById(id).orElseThrow { IllegalArgumentException("THREAD_NOT_FOUND") }

    @Transactional
    fun toggleBookmark(threadId: Long, userId: Long): Boolean {
        val id = com.rev.app.domain.community.BookmarkId(userId, threadId)
        val exists = bookmarks.findById(id).isPresent
        if (exists) { bookmarks.deleteById(id); return false }
        bookmarks.save(Bookmark(userId = userId, threadId = threadId))
        return true
    }

    @Transactional
    fun reactThread(threadId: Long, userId: Long, req: ReactionReq): ThreadReaction {
        val existing = threadReactions.findByUserIdAndThreadId(userId, threadId)
        existing.forEach { threadReactions.delete(it) }
        return threadReactions.save(ThreadReaction(threadId = threadId, userId = userId, kind = req.kind))
    }
}

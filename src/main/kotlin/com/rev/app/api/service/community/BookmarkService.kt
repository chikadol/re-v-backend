package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BookmarkCountRes
import com.rev.app.api.service.community.dto.BookmarkToggleRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BookmarkService(
    private val threadRepo: ThreadRepository,
    private val bookmarkRepo: ThreadBookmarkRepository,
    private val userRepo: UserRepository
) {

    fun toggle(userId: UUID, threadId: UUID): BookmarkToggleRes {
        // 이미 북마크 했는지 확인
        val existing = bookmarkRepo.findByThread_IdAndUser_Id(threadId, userId)

        return if (existing != null) {
            // 이미 북마크 있으면 삭제 = 토글 OFF
            bookmarkRepo.delete(existing)
            val count = bookmarkRepo.countByThread_Id(threadId)
            BookmarkToggleRes(
                toggled = false,
                count = count
            )
        } else {
            // 없으면 새로 북마크 추가 = 토글 ON
            val thread = threadRepo.getReferenceById(threadId)
            val user = userRepo.getReferenceById(userId)

            // ✅ 여기 생성자는 네 프로젝트의 ThreadBookmarkEntity 정의에 맞게 조금 수정해야 할 수도 있어
            val bookmark = ThreadBookmarkEntity(
                thread = thread,
                user = user
            )

            bookmarkRepo.save(bookmark)
            val count = bookmarkRepo.countByThread_Id(threadId)
            BookmarkToggleRes(
                toggled = true,
                count = count
            )
        }
    }

    fun countThreadBookmarks(threadId: UUID): BookmarkCountRes {
        val count = bookmarkRepo.countByThread_Id(threadId)
        return BookmarkCountRes(count = count)
    }

    fun listMyBookmarks(userId: UUID, pageable: Pageable): Page<ThreadRes> {
        // ThreadBookmarkEntity 에 thread, user, createdAt 이런 필드가 있다고 가정
        val bookmarks = bookmarkRepo.findAllByUser_IdWithThreadAndBoard(userId, pageable)

        return bookmarks.map { bookmark ->
            val thread = bookmark.thread
            requireNotNull(thread) { "Bookmark.thread 가 null 입니다." }

            ThreadRes.from(thread)
        }
    }
}

package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.BookmarkCountRes
import com.rev.app.api.service.community.dto.BookmarkToggleRes
import com.rev.app.api.service.community.dto.MyBookmarkedThreadRes
import com.rev.app.api.service.community.dto.toMyBookmarkedThreadRes
import com.rev.app.api.service.community.dto.ThreadRes
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.ThreadBookmarkEntity
import com.rev.app.domain.community.repo.ThreadBookmarkRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BookmarkService(
    private val threadRepo: ThreadRepository,
    private val bookmarkRepo: ThreadBookmarkRepository,
    private val userRepo: UserRepository
) {

    @Transactional
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

    fun listMyBookmarks(userId: UUID, pageable: Pageable): Page<MyBookmarkedThreadRes> {
        return try {
            val bookmarks = bookmarkRepo.findAllByUser_IdWithThreadAndBoard(userId, pageable)
            bookmarks.map { bookmark ->
                // LAZY 로딩된 필드들을 명시적으로 접근하여 로드
                bookmark.thread?.id
                bookmark.thread?.board?.id
                bookmark.thread?.board?.name
                bookmark.toMyBookmarkedThreadRes()
            }
        } catch (e: Exception) {
            // 테이블이 없거나 에러가 발생하면 빈 페이지 반환
            PageImpl(emptyList(), pageable, 0)
        }
    }
}

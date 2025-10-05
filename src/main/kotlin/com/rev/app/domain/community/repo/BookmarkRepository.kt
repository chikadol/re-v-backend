package com.rev.app.domain.community.repo

import com.rev.app.domain.community.Bookmark
import com.rev.app.domain.community.BookmarkId
import org.springframework.data.jpa.repository.JpaRepository

interface BookmarkRepository : JpaRepository<Bookmark, BookmarkId>

package com.rev.app.api.community.dto

import com.rev.app.domain.community.*

fun boardDto(b: Board) = BoardDto(
    id = b.id!!, slug = b.slug, name = b.name,
    isAnonymousAllowed = b.isAnonymousAllowed, createdAt = b.createdAt
)

fun threadDto(t: Thread) = ThreadDto(
    id = t.id!!, boardId = t.boardId, authorId = t.authorId,
    title = t.title, displayNo = t.displayNo, createdAt = t.createdAt,
    updatedAt = t.updatedAt, deletedAt = t.deletedAt
)

fun threadDetailDto(t: Thread) = ThreadDetailDto(
    id = t.id!!, boardId = t.boardId, authorId = t.authorId, title = t.title,
    content = t.content, isAnonymous = t.isAnonymous, displayNo = t.displayNo,
    viewCount = t.viewCount, likeCount = t.likeCount, dislikeCount = t.dislikeCount,
    commentCount = t.commentCount, pinnedUntil = t.pinnedUntil, deletedAt = t.deletedAt,
    createdAt = t.createdAt, updatedAt = t.updatedAt
)

fun commentDto(c: Comment) = CommentDto(
    id = c.id!!, threadId = c.threadId, parentId = c.parentId, authorId = c.authorId,
    content = c.content, isAnonymous = c.isAnonymous, likeCount = c.likeCount,
    deletedAt = c.deletedAt, createdAt = c.createdAt
)

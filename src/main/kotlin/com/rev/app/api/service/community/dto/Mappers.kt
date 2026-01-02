package com.rev.app.api.service.community.dto

import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.entity.ThreadBookmarkEntity

fun CommentEntity.toMyCommentRes(): MyCommentRes =
    MyCommentRes(
        commentId = requireNotNull(id),
        threadId = requireNotNull(thread.id),
        threadTitle = thread.title,
        boardId = thread.board?.id,
        boardName = thread.board?.name,
        content = content,
        createdAt = createdAt
    )
fun ThreadBookmarkEntity.toMyBookmarkedThreadRes(): MyBookmarkedThreadRes =
    MyBookmarkedThreadRes(
        threadId = requireNotNull(thread.id),
        title = thread.title,
        boardId = thread.board?.id,
        boardName = thread.board?.name,
        createdAt = thread.createdAt,   // ✅ ThreadEntity 쪽 필드만 사용
        bookmarkedAt = null             // ✅ 북마크 엔티티에 createdAt 없으니 일단 null
    )
fun Board.toRes(): BoardRes = BoardRes(
    id = requireNotNull(id),
    name = name,
    slug = slug,
    description = description
)
fun ThreadEntity.toRes(): ThreadRes = ThreadRes(
    id = requireNotNull(id),
    title = title,
    content = content,
    boardId = board?.id,
    parentThreadId = parent?.id,
    authorId = null,               // 익명 처리
    isPrivate = isPrivate,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = emptyList()            // ✅ 기본값 유지
)

fun ThreadEntity.toResWithTags(tags: List<String>): ThreadRes =
    this.toRes().copy(tags = tags)

fun CommentEntity.toRes(): CommentRes {
    // 게시물 작성자와 댓글 작성자가 같은지 확인
    val threadAuthorId = thread?.author?.id
    val commentAuthorId = author?.id
    val isAuthor = threadAuthorId != null && 
                   commentAuthorId != null && 
                   threadAuthorId == commentAuthorId
    
    return CommentRes(
        id = requireNotNull(id),
        threadId = requireNotNull(thread?.id),
        authorId = null, // 익명 처리
        parentId = parent?.id,
        content = content,
        createdAt = createdAt,
        isAuthor = isAuthor
    )
}

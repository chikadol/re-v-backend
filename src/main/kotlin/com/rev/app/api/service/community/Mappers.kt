package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.*
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.api.service.community.dto.ThreadRes
fun Board.toRes(): BoardRes =
    BoardRes(
        id = requireNotNull(id),
        name = name,
        slug = slug,
        description = description
    )

fun ThreadEntity.toRes(): ThreadRes =
    ThreadRes(
        id = requireNotNull(id),
        title = title,
        content = content,
        boardId = board?.id,                   // ✅ boardId 채워줌
        parentThreadId = parent?.id,
        authorId = try { author?.id } catch (_: Exception) { null },
        isPrivate = try { isPrivate ?: false } catch (_: Exception) { false },
        categoryId = try { categoryId } catch (_: Exception) { null },
        createdAt = null,   // ✅ 엔티티에 필드가 없으므로 일단 null
        updatedAt = null,   // ✅ 엔티티에 필드가 없으므로 일단 null
        tags = try { tags ?: emptyList() } catch (_: Exception) { emptyList() }
    )

fun CommentEntity.toRes(): CommentRes =
    CommentRes(
        id = requireNotNull(id),
        threadId = thread?.id,
        authorId = author?.id,
        parentId = parent?.id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

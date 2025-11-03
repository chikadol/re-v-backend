package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CommentService(
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun create(me: JwtPrincipal, threadId: Long, content: String, parentId: Long?): CommentRes {
        val author = userRepository.getReferenceById(requireNotNull(me.userId))
        val thread: ThreadEntity = threadRepository.getReferenceById(threadId)
        val parent = parentId?.let { commentRepository.getReferenceById(it) }

        val saved = commentRepository.save(
            CommentEntity(
                thread = thread,
                author = author,
                content = content,
                parent = parent,          // ✅ parent는 엔티티
            )
        )
        return saved.toRes()
    }

    @Transactional
    fun listThreadComments(threadId: Long): List<CommentRes> {
        val roots = commentRepository.findByThread_IdAndParentIsNullOrderByIdAsc(threadId)
        // 간단한 2단 트리(루트 + 자식)
        val res = mutableListOf<CommentRes>()
        roots.forEach { r ->
            res += r.toRes()
            val children = commentRepository.findByParent_IdOrderByIdAsc(requireNotNull(r.id))
            res += children.map { it.toRes() }
        }
        return res
    }
}

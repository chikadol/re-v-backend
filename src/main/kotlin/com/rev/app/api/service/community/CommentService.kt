package com.rev.app.api.service.community

import com.rev.app.api.security.JwtPrincipal
import com.rev.app.api.service.community.dto.CommentRes
import com.rev.app.api.service.community.dto.CreateCommentReq
import com.rev.app.auth.UserRepository
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.CommentRepository
import com.rev.app.domain.community.repo.ThreadRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun addComment(me: JwtPrincipal, threadId: Long, req: CreateCommentReq): CommentRes {
        val thread: ThreadEntity = threadRepository.findById(threadId)
            .orElseThrow { NoSuchElementException("thread $threadId not found") }
        val author = userRepository.getReferenceById(requireNotNull(me.userId))
        val parentEntity = req.parentId?.let { commentRepository.findById(it).orElse(null) }

        val saved = commentRepository.save(
            CommentEntity(
                thread = thread,
                author = author,
                content = req.content,
                parent = parentEntity
            )
        )
        return saved.toRes()
    }

    @Transactional(readOnly = true)
    fun listRootComments(threadId: Long, pageable: Pageable): List<CommentRes> =
        commentRepository.findByThread_IdAndParentIsNullOrderByIdAsc(threadId, pageable).map { it.toRes() }

    @Transactional(readOnly = true)
    fun listChildren(parentId: Long): List<CommentRes> =
        commentRepository.findByParent_IdOrderByIdAsc(parentId).map { it.toRes() }
}

package com.rev.app.api.service.community

import com.rev.app.api.service.community.dto.CreateThreadReq
import com.rev.app.api.service.community.dto.UpdateThreadReq
import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.ThreadRepository
import com.rev.app.auth.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

interface ThreadService {
    fun create(authorId: UUID, req: CreateThreadReq): ThreadEntity
    fun get(id: Long): ThreadEntity
    fun listPublic(pageable: Pageable): Page<ThreadEntity>
    fun update(id: Long, actorId: UUID, req: UpdateThreadReq): ThreadEntity
    fun delete(id: Long, actorId: UUID)
}

@Service
class ThreadServiceImpl(
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository
) : ThreadService {

    override fun create(authorId: UUID, req: CreateThreadReq): ThreadEntity {
        val author = userRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("Author not found") }

        val entity = ThreadEntity(
            title = req.title,
            content = req.content,
            author = author,
            tags = req.tags.toMutableList(),
            categoryId = req.categoryId,
            parentId = req.parentThreadId,
            isPrivate = req.isPrivate
        )
        return threadRepository.save(entity)
    }

    override fun get(id: Long): ThreadEntity =
        threadRepository.findById(id).orElseThrow { NoSuchElementException("Thread $id not found") }

    override fun listPublic(pageable: Pageable): Page<ThreadEntity> =
        threadRepository.findByIsPrivateFalse(pageable)

    override fun update(id: Long, actorId: UUID, req: UpdateThreadReq): ThreadEntity {
        val e = get(id)
        if (e.author.id != actorId) throw IllegalArgumentException("Only author can update the thread")
        req.title?.let { e.title = it }
        req.content?.let { e.content = it }
        req.tags?.let { e.tags = it.toMutableList() }
        e.categoryId = req.categoryId
        req.isPrivate?.let { e.isPrivate = it }
        return threadRepository.save(e)
    }

    override fun delete(id: Long, actorId: UUID) {
        val e = get(id)
        if (e.author.id != actorId) throw IllegalArgumentException("Only author can delete the thread")
        threadRepository.delete(e)
    }
}

package com.rev.app.api.service.community

import com.rev.app.domain.community.entity.ThreadEntity
import com.rev.app.domain.community.repo.CommunityThreadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
interface ThreadService {
    fun get(id: Long): ThreadEntity
    fun create(entity: ThreadEntity): ThreadEntity
}
@Service
@Transactional(readOnly = true)
class ThreadServiceImpl(
    private val threadRepo: CommunityThreadRepository
) : ThreadService {

    override fun get(id: Long): ThreadEntity =
        threadRepo.findById(id).orElseThrow { NoSuchElementException("thread $id") }

    @Transactional
    override fun create(entity: ThreadEntity): ThreadEntity =
        threadRepo.save(entity)
}

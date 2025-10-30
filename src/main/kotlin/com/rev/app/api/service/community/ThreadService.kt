package com.rev.app.api.service.community


import com.rev.app.api.service.community.dto.ThreadDto
import org.springframework.stereotype.Service

import java.util.UUID


@Service
class ThreadService {
    fun create(authorId: Long, req: CreateThreadReq): ThreadDto {
        throw NotImplementedError("TODO: implement")
    }
    fun get(userId: Long, threadId: Long): ThreadDto {
        throw NotImplementedError("TODO: implement")
    }
    fun reactToThread(userId: Long, threadId: Long, type: ReactionType): ThreadDto {
        throw NotImplementedError("TODO: implement")
    }
}
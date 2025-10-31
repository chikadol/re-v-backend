package com.rev.app.domain.community.repo

import com.rev.app.domain.community.ThreadReaction
import com.rev.app.api.service.community.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

import java.util.UUID

interface ThreadReactionRepository : JpaRepository<ThreadReaction, Long> {

    fun findByThread_IdAndUser_Id(threadId: Long, userId: UUID): List<ThreadReaction>

    // 카운트용: 필드명이 reaction 이라면 아래처럼!
    fun countByThread_IdAndReaction(threadId: Long, reaction: ReactionType): Long

    // 있으면 편한 존재 여부
    fun existsByThread_IdAndUser_IdAndReaction(threadId: Long, userId: UUID, reaction: ReactionType): Boolean
}

package com.rev.app.domain.community

import com.rev.app.api.service.community.ReactionType
import com.rev.app.auth.UserEntity
import com.rev.app.common.jpa.BaseTime
import com.rev.app.domain.community.entity.ThreadEntity
import jakarta.persistence.*
import java.time.Instant

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread_reaction", schema = "rev")
open class ThreadReaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction", nullable = false)   // ← 필드명이 reaction이라고 가정
    var reaction: ReactionType
)

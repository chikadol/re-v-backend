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
class ThreadReaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity,

    // 사용자 매핑: UserEntity로 매핑한 경우
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    // 필드명이 'reactionType'이라고 가정
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 20)
    var reactionType: ReactionType
)


package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.common.jpa.BaseTime
import jakarta.persistence.*

@Entity
@Table(name = "thread_bookmark", schema = "rev")
open class ThreadBookmarkEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
) : BaseTime()

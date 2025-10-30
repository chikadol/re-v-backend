// src/main/kotlin/com/rev/app/domain/community/entity/ThreadBookmarkEntity.kt
package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "thread_bookmark", schema = "rev")
open class ThreadBookmarkEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    open var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: UserEntity,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now()
)

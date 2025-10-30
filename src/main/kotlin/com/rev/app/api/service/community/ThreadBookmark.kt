package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "thread_bookmark",
    uniqueConstraints = [UniqueConstraint(columnNames = ["thread_id", "user_id"])]
)
data class ThreadBookmark(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "thread_id", nullable = false)
    val threadId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

/*    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()*/
)

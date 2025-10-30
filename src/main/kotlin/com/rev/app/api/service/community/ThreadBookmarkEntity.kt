package com.rev.app.api.service.community

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    schema = "rev",
    name = "thread_bookmark",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_thread_bookmark_thread_user", columnNames = ["thread_id", "user_id"])
    ]
)
class ThreadBookmarkEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(name = "thread_id", nullable = false)
    var threadId: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

/*    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()*/
)
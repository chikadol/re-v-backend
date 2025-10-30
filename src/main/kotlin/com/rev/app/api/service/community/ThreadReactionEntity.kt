package com.rev.app.api.service.community

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    schema = "rev",
    name = "thread_reaction",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_thread_reaction_thread_user", columnNames = ["thread_id", "user_id"])
    ]
)
class ThreadReactionEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(name = "thread_id", nullable = false)
    var threadId: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    var type: ReactionType,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
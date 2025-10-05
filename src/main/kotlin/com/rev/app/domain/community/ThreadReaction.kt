package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "thread_reaction", schema = "rev")
class ThreadReaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false) var threadId: Long,
    @Column(nullable = false) var userId: Long,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var kind: ReactionKind,
    var createdAt: Instant = Instant.now()
)

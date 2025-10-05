package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "comment_reaction", schema = "rev")
class CommentReaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var commentId: Long,
    @Column(nullable = false)
    var userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: ReactionKind,
    var createdAt: Instant = Instant.now()
)

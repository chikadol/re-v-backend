package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

enum class ModerationAction { PIN, UNPIN, HIDE, DELETE, RESTORE }

@Entity @Table(name = "thread_moderation_log", schema = "rev")
class ThreadModerationLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var threadId: Long,
    @Column(nullable = false) var actorUserId: Long,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var action: ModerationAction,
    @Column(columnDefinition = "text") var reason: String? = null,
    var createdAt: Instant = Instant.now()
)

@Entity @Table(name = "comment_moderation_log", schema = "rev")
class CommentModerationLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var commentId: Long,
    @Column(nullable = false) var actorUserId: Long,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var action: ModerationAction,
    @Column(columnDefinition = "text") var reason: String? = null,
    var createdAt: Instant = Instant.now()
)

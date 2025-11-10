package com.rev.app.domain.notification

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.CommentEntity
import com.rev.app.domain.community.entity.ThreadEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notification", schema = "rev")
class NotificationEntity(
    @Id @GeneratedValue @UuidGenerator @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    var user: UserEntity,

    @Column(nullable = false, length = 30)
    var type: String, // "COMMENT"

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "thread_id")
    var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "comment_id")
    var comment: CommentEntity,

    @Column(nullable = false, columnDefinition = "text")
    var message: String,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)

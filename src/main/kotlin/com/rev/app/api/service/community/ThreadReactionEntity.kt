package com.rev.app.domain.community.model

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.entity.ThreadEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "thread_reaction", schema = "rev",
    uniqueConstraints = [UniqueConstraint(columnNames = ["thread_id","user_id","type"])]
)
class ThreadReactionEntity(
    @Id @GeneratedValue @UuidGenerator @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "thread_id")
    var thread: ThreadEntity?,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    var user: UserEntity?,

    @Column(nullable = false, length = 20)
    var type: String, // "LIKE", "LOVE"...

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)

package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "comment", schema = "rev")
class CommentEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    var author: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: CommentEntity? = null,

    @Column(nullable = false, columnDefinition = "text")
    var content: String,

    var createdAt: Instant? = Instant.now()
)

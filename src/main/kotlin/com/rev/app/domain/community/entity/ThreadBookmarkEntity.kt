package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(
    name = "thread_bookmark",
    schema = "rev",
    uniqueConstraints = [UniqueConstraint(columnNames = ["thread_id", "user_id"])]
)
class ThreadBookmarkEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity
)

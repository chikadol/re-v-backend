package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread_bookmark", schema = "rev",
    uniqueConstraints = [UniqueConstraint(columnNames = ["thread_id","user_id"])])
class ThreadBookmarkEntity(
    @Id @GeneratedValue
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "thread_id")
    var thread: ThreadEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    var user: UserEntity? = null
)

package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import com.rev.app.domain.community.model.ThreadTagEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
class ThreadEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "text")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    var board: Board? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: ThreadEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    var author: UserEntity? = null,

    @Column(nullable = false)
    var isPrivate: Boolean = false,

    @JdbcTypeCode(SqlTypes.UUID)
    var categoryId: UUID? = null,

    var createdAt: Instant? = Instant.now(),
    var updatedAt: Instant? = Instant.now(),

    @ElementCollection
    @CollectionTable(name = "thread_tags", schema = "rev", joinColumns = [JoinColumn(name = "thread_id")])
    @Column(name = "tag")
    var tags: List<String>? = emptyList(),

    @OneToMany(mappedBy = "thread")
var tagLinks: MutableSet<ThreadTagEntity> = mutableSetOf()
)

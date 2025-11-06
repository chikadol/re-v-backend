package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
class ThreadEntity(
    @Id @GeneratedValue
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "text")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    var board: Board? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    var author: UserEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: ThreadEntity? = null,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column
    var categoryId: UUID? = null,

    @CreationTimestamp
    var createdAt: Instant? = null,

    @UpdateTimestamp
    var updatedAt: Instant? = null,

    @ElementCollection
    @Column(name = "tag", nullable = false)
    var tags: List<String> = emptyList()
)

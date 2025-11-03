package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
class ThreadEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "text")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity? = null
,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    var board: Board,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column(name = "category_id")
    var categoryId: UUID? = null,

    @Column(name = "parent_id")
    var parentId: Long? = null,

    @ElementCollection
    @CollectionTable(name = "thread_tags", schema = "rev", joinColumns = [JoinColumn(name = "thread_id")])
    @Column(name = "tag")
    var tags: List<String>? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)

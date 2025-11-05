package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
class ThreadEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
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

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "category_id")
    var categoryId: UUID? = null,

    @ElementCollection
    @CollectionTable(schema = "rev", name = "thread_tags", joinColumns = [JoinColumn(name = "thread_id")])
    @Column(name = "tag", nullable = false)
    var tags: List<String> = emptyList()
)

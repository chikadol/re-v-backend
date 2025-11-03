package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.common.jpa.BaseTime
import com.rev.app.domain.community.Board
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
open class ThreadEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "text")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity?,

    @ElementCollection
    @CollectionTable(name = "thread_tags", schema = "rev", joinColumns = [JoinColumn(name = "thread_id")])
    @Column(name = "tag")
    var tags: MutableList<String> = mutableListOf(),

    @Column(name = "category_id")
    var categoryId: UUID? = null,

    @Column(name = "parent_id")
    var parentId: Long? = null,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    var board: Board? = null,
) : BaseTime()

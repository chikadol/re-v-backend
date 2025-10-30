package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
class ThreadEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 200)
    var title: String,

    @Lob
    @Column(nullable = false)
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        schema = "rev",
        name = "thread_tag",
        joinColumns = [JoinColumn(name = "thread_id")]
    )
    @Column(name = "tag", length = 50)
    var tags: MutableList<String> = mutableListOf(),

    @Column(name = "category_id")
    var categoryId: UUID? = null,

    @Column(name = "parent_thread_id")
    var parentThreadId: UUID? = null,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,


)
package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.common.jpa.BaseTime
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction


@Entity
@Table(name = "comment", schema = "rev")
@SQLRestriction("deleted = false")
open class CommentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @Column(nullable = false)
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: CommentEntity? = null,

    @Column(nullable = false)
    var deleted: Boolean = false
) : BaseTime()

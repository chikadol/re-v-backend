package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.common.jpa.BaseTime
import jakarta.persistence.*

@Entity
@Table(name = "comment", schema = "rev")
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

    // 🔽 연관관계 제거하고 숫자 FK로만 보관
    @Column(name = "parent_id")
    var parentId: Long? = null
) : BaseTime()

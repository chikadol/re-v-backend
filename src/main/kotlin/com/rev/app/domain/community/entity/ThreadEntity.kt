package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
@Access(AccessType.FIELD) // 필드 접근만 사용해서 getter 기반 매핑 방지
open class ThreadEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 200)
    var title: String,

    @Lob
    @Column(nullable = false)
    var content: String,

    // ✅ author_id는 여기 하나만 매핑
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false) // 여기서만 author_id 매핑
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
    var parentId: UUID? = null,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,
)

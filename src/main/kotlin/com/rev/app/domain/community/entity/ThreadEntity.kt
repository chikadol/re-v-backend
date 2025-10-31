// src/main/kotlin/com/rev/app/domain/community/entity/ThreadEntity.kt
package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.common.jpa.BaseTime
import jakarta.persistence.*

@Entity
@Table(name = "thread", schema = "rev")
open class ThreadEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @CollectionTable(schema = "rev", name = "thread_tag", joinColumns = [JoinColumn(name = "thread_id")])
    @Column(name = "tag", length = 50)
    var tags: MutableList<String> = mutableListOf(),

    @Column(name = "category_id")
    var categoryId: java.util.UUID? = null,

    @Column(name = "parent_thread_id")
    var parentThreadId: Long? = null,   // ← 실제 타입에 맞춰 두세요(예: Long? 또는 UUID?)

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,
) : BaseTime()   // ✅ BaseTime 상속

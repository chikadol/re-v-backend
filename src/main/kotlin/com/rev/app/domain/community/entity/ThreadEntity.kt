package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "thread", schema = "rev")
open class ThreadEntity(
    title: String,
    content: String,
    author: UserEntity,
    tags: MutableList<String>,
    categoryId: UUID?,
    parentThreadId: UUID?,
    isPrivate: Boolean
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, length = 200)
    var title: String = ""

    @Lob
    @Column(nullable = false)
    var content: String = ""

    /** 작성자 FK — 물리 컬럼은 여기서만 매핑(중복 금지) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    lateinit var author: UserEntity


    /** 태그 컬렉션 (rev.thread_tag.thread_id FK → thread.id) */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        schema = "rev",
        name = "thread_tag",
        joinColumns = [JoinColumn(name = "thread_id")]
    )
    @Column(name = "tag", length = 50, nullable = false)
    var tags: MutableList<String> = mutableListOf()

    /** 카테고리: 아직 엔티티가 없으면 UUID 컬럼으로만 보유 */
    @Column(name = "category_id")
    var categoryId: UUID? = null

    /** 부모 스레드: 자기참조. (parent_thread_id → thread.id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_thread_id")
    var parent: ThreadEntity? = null

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false}

    /** 타임스탬프 (Flyway로 컬럼 이미 존재 가정) */
/*
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
*/

/*    @PrePersist
    fun onCreate() {
        val now = OffsetDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}*/

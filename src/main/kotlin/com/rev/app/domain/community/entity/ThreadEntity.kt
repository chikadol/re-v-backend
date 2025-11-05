// src/main/kotlin/com/rev/app/domain/community/entity/ThreadEntity.kt
package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.domain.community.Board               // ✅ 엔티티 임포트
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
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

    // ✅ 연관관계는 엔티티 Board 를 가리켜야 함 (절대 BoardRes 아님)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    var board: Board? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    var author: UserEntity? = null,

    // self FK: parent_id (uuid, ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: ThreadEntity? = null,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "category_id")
    var categoryId: UUID? = null,

    @Column(name = "created_at")
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    // 태그 컬럼/조인테이블 구조에 맞게 매핑해두셨다면 그에 맞춰 유지
    @ElementCollection
    @CollectionTable(
        name = "thread_tags",
        schema = "rev",
        joinColumns = [JoinColumn(name = "thread_id")]
    )
    @Column(name = "tag", nullable = false)
    var tags: List<String> = emptyList()
)

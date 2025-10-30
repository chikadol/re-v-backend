package com.rev.app.domain.community.entity

import com.rev.app.auth.UserEntity
import com.rev.app.common.BaseTime
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "comment", schema = "rev")
open class CommentEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // FK: rev.comment.thread_id -> thread.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    var thread: ThreadEntity,

    // FK: rev.comment.author_id -> user.id (UUID)
    // ⚠️ author_id 컬럼은 여기 'author'에만 매핑 (authorId 같은 별도 필드 만들지 마세요!)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @Lob
    @Column(nullable = false)
    var content: String,

    // 대댓글(선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: CommentEntity? = null,

) : BaseTime()

package com.rev.app.domain.community

import com.rev.app.auth.UserEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "comment", schema = "rev")
class Comment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false) var threadId: Long,
    var parentId: Long? = null,
    @Column(nullable = false) var authorId: Long,
    @Column(columnDefinition = "text", nullable = false) var content: String,
    var isAnonymous: Boolean = false,
    var likeCount: Int = 0,
    var deletedAt: Instant? = null,
    var createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity
)

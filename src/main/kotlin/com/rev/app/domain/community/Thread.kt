package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "thread", schema = "rev")
class Thread(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false) var boardId: Long,
    @Column(nullable = false) var authorId: Long,
    @Column(nullable = false) var title: String,
    @Column(columnDefinition = "text") var content: String? = null,
    var isAnonymous: Boolean = false,
    @Column(nullable = false) var displayNo: Long,
    var viewCount: Long = 0,
    var likeCount: Int = 0,
    var dislikeCount: Int = 0,
    var commentCount: Int = 0,
    var pinnedUntil: Instant? = null,
    var deletedAt: Instant? = null,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

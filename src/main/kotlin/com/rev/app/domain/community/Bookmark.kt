package com.rev.app.domain.community

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "bookmark", schema = "rev")
@IdClass(BookmarkId::class)
class Bookmark(
    @Id var userId: Long = 0,
    @Id var threadId: Long = 0,
    var createdAt: Instant = Instant.now()
)
data class BookmarkId(var userId: Long = 0, var threadId: Long = 0): Serializable

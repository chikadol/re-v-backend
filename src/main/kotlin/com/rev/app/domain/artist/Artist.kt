package com.rev.app.domain.artist

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "artist", schema = "rev")
class Artist(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var stageName: String,
    var stageNameKr: String? = null,
    var groupName: String? = null,
    @Column(columnDefinition = "text[]")
    var tags: Array<String>? = null,
    var debutDate: LocalDate? = null,
    var avatarUrl: String? = null,
    var popularityScore: Int = 0,
    var createdAt: Instant = Instant.now()
)

package com.rev.app.domain.genba

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "genba", schema = "rev")
class Genba(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var title: String,
    @Column(columnDefinition = "text")
    var description: String? = null,
    var startAt: Instant,
    var endAt: Instant? = null,
    var areaCode: String? = null,
    var placeName: String? = null,
    var address: String? = null,
    var posterUrl: String? = null,
    var popularityScore: Int = 0,
    var createdAt: Instant = Instant.now()
)

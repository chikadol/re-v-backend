package com.rev.app.domain.community

import jakarta.persistence.*
import java.time.Instant

@Entity @Table(name = "board", schema = "rev")
class Board(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false, unique = true) var slug: String,
    @Column(nullable = false) var name: String,
    var isAnonymousAllowed: Boolean = true,
    var isPrivate: Boolean = false,
    @Column(columnDefinition = "jsonb") var rules: String? = "{}",
    var createdAt: Instant = Instant.now()
)

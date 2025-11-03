package com.rev.app.domain.community

import jakarta.persistence.*

@Entity
@Table(name = "board", schema = "rev")
class Board(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null
)

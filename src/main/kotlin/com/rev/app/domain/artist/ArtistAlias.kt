package com.rev.app.domain.artist

import jakarta.persistence.*

@Entity
@Table(name = "artist_alias", schema = "rev")
class ArtistAlias(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var artistId: Long,
    var alias: String
)

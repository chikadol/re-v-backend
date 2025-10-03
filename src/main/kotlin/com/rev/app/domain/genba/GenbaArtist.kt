package com.rev.app.domain.genba

import jakarta.persistence.*

@Entity
@Table(name = "genba_artist", schema = "rev")
@IdClass(GenbaArtistId::class)
class GenbaArtist(
    @Id var genbaId: Long = 0,
    @Id var artistId: Long = 0
)

data class GenbaArtistId(
    var genbaId: Long = 0,
    var artistId: Long = 0
) : java.io.Serializable

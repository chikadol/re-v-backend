package com.rev.app.domain.community

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "board", schema = "rev")
class Board(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    var description: String? = null
)

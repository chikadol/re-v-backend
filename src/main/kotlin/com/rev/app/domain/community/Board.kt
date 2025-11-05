package com.rev.app.domain.community

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "board", schema = "rev",
    uniqueConstraints = [UniqueConstraint(name = "uk_board_slug", columnNames = ["slug"])])
class Board(

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 120)
    var slug: String,

    @Column(nullable = true, length = 500)
    var description: String? = null
)

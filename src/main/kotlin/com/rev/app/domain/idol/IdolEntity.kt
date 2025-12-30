package com.rev.app.domain.idol

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "idol", schema = "rev")
class IdolEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column
    var imageUrl: String? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)


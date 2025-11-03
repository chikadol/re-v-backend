
package com.rev.app.auth

import jakarta.persistence.*
import java.util.UUID


@Table(schema = "rev", name = "\"user\"")
@Entity
class UserEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false, unique = true)
    var email: String
)

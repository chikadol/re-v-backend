package com.rev.app.core.user

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "user", schema = "rev")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false)
    var passwordHash: String,
    var status: String = "ACTIVE",
    var createdAt: Instant = Instant.now()
)

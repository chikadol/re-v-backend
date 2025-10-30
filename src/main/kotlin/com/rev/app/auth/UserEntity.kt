package com.rev.app.auth

import jakarta.persistence.*

import java.time.Instant

@Entity
@Table(name = "app_user")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false)
    var password: String = "",

    var createdAt: Instant = Instant.now()
)
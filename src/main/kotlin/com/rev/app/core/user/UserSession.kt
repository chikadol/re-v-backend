package com.rev.app.core.user

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "user_session", schema = "rev")
class UserSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var userId: Long,
    var refreshTokenHash: String,
    var expiresAt: Instant,
    var createdAt: Instant = Instant.now()
)

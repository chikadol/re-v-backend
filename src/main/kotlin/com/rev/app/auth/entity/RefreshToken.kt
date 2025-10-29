package com.rev.app.auth.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "refresh_token", schema = "rev")
class RefreshToken(
    @Id @GeneratedValue var id: UUID? = null,
    var subject: String = "",
    @Column(name="token_hash", unique = true) var tokenHash: String = "",
    @Column(name="expires_at") var expiresAt: Instant = Instant.now(),
    var revoked: Boolean = false,
    @Column(name="created_at") var createdAt: Instant = Instant.now()
)

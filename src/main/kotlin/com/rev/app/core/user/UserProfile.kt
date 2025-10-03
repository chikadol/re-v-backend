package com.rev.app.core.user

import jakarta.persistence.*

@Entity
@Table(name = "user_profile", schema = "rev")
class UserProfile(
    @Id
    var userId: Long,
    var nickname: String? = null,
    var avatarUrl: String? = null
)

package com.rev.app.auth

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "users", schema = "rev")
class UserEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = true)
    var provider: String? = null, // 'google', 'naver', 'kakao', null (일반 회원가입)

    @Column(nullable = true, unique = true)
    var providerId: String? = null, // OAuth2 provider의 사용자 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var role: UserRole = UserRole.USER // USER, IDOL
)

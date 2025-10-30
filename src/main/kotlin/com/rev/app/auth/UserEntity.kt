package com.rev.app.auth

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_user", schema = "rev") // 실제 테이블명으로 변경
class UserEntity(

    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 190)
    var username: String,

    // 실제 컬럼명이 email이 아니라면 name에 정확히 써주기 (예: user_email, email_address 등)
    @Column(name = "email", nullable = false, unique = true, length = 320)
    var email: String,

    @Column(nullable = false)
    var password: String,

)

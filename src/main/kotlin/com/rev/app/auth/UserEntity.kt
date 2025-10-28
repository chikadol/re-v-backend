package com.rev.app.auth

import jakarta.persistence.*


@Entity
@Table(name = "user", schema = "rev")
data class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true)
    val email: String,
    val password: String,
    /**
     * 콤마구분(예: "USER,ADMIN")
     */
    val roles: String = "USER",
)
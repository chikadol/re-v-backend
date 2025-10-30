package com.rev.app.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    lateinit var secret: String         // 32자 이상
    var accessTtlSeconds: Long = 900    // 15분 기본
    var refreshTtlSeconds: Long = 1209600 // 14일 기본
    var header: String = "Authorization"
    var prefix: String = "Bearer "
}

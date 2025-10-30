// com.rev.app.auth.jwt.JwtProperties.kt
package com.rev.app.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val issuer: String = "rev-app",
    // 최소 32자 이상으로 기본값 제공 (개발용, 운영에서는 반드시 환경변수/시크릿으로 교체)
    val secret: String = "d0c41382-cae7-44c3-a401-77828c270ab3",
    val accessTtlSeconds: Long = 900,        // 15분
    val refreshTtlSeconds: Long = 2592000    // 30일
)

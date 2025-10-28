package com.rev.app.auth.jwt


import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "app.jwt")
class JwtProps {
    lateinit var secret: String
    lateinit var issuer: String
    var accessTokenMinutes: Long = 15
    var refreshTokenDays: Long = 7
}
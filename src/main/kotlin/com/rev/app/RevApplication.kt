package com.rev.app

import com.rev.app.auth.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("com.rev.app")
class RevApplication


fun main(args: Array<String>) {
    runApplication<RevApplication>(*args)
}

package com.rev.app

import com.rev.app.auth.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@ConfigurationPropertiesScan("com.rev.app")
@SpringBootApplication(scanBasePackages = ["com.rev.app"])
@EntityScan(basePackages = ["com.rev.app"])
@EnableJpaRepositories(basePackages = ["com.rev.app"])
class RevApplication


fun main(args: Array<String>) {
    runApplication<RevApplication>(*args)
}

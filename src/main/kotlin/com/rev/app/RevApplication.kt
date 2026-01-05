package com.rev.app

import com.rev.app.auth.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication(
    scanBasePackages = ["com.rev.app"],
    exclude = [
        org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration::class,
        // Redis가 없어도 애플리케이션이 시작되도록 자동 구성 제외 (선택적)
        // org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration::class
    ]
)
@ConfigurationPropertiesScan
@EnableConfigurationProperties(JwtProperties::class)
class RevApplication

fun main(args: Array<String>) {
    runApplication<RevApplication>(*args)
}

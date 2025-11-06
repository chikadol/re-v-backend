package com.rev.app.repo

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestFlywayConfig {
    @Bean
    fun strategy(): FlywayMigrationStrategy = FlywayMigrationStrategy { flyway: Flyway ->
        flyway.clean()   // ✅ 매번 깨끗이
        flyway.migrate() // ✅ 최신까지 적용
    }
}

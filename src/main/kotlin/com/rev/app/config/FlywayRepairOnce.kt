package com.rev.app.config

import org.flywaydb.core.Flyway
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayRepairOnce {
    @Bean
    fun flywayRepairRunner(flyway: Flyway) = ApplicationRunner {
        // V24 체크섬 불일치 때문에 막히는 경우, 한 번 repair
        flyway.repair()
    }
}
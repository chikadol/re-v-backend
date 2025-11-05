// src/test/kotlin/com/rev/app/support/pg/PostgresTC.kt
package com.rev.app.support.pg

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class PostgresTC {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url") { postgres.jdbcUrl }
            reg.add("spring.datasource.username") { postgres.username }
            reg.add("spring.datasource.password") { postgres.password }
            reg.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }

            reg.add("spring.flyway.enabled") { "false" }
            reg.add("spring.sql.init.mode") { "never" } // ★ 스프링 기본 초기화 비활성화

            reg.add("spring.jpa.hibernate.ddl-auto") { "update" }
            reg.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces") { "true" }
            reg.add("spring.jpa.properties.hibernate.default_schema") { "rev" }
        }
    }
}

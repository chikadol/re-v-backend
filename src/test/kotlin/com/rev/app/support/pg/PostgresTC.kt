package com.rev.app.support.pg

import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

abstract class PostgresTC {
    companion object {
        private val pg = PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url") { pg.jdbcUrl }
            reg.add("spring.datasource.username") { pg.username }
            reg.add("spring.datasource.password") { pg.password }
            reg.add("spring.jpa.hibernate.ddl-auto") { "update" }
        }

        @BeforeAll @JvmStatic
        fun started() { /* ensure static init */ }
    }
}

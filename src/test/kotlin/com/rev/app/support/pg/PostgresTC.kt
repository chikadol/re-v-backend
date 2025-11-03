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
            // ⛔ init.sql 호출 제거
            // withInitScript("testdb/init.sql")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url") { pg.jdbcUrl }
            reg.add("spring.datasource.username") { pg.username }
            reg.add("spring.datasource.password") { pg.password }

            // ✅ 테스트에서 스키마/테이블 자동 생성
            reg.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            reg.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces") { "true" }
            reg.add("spring.jpa.properties.hibernate.default_schema") { "rev" }
        }

        @BeforeAll @JvmStatic
        fun started() { /* ensure static init */ }
    }
}

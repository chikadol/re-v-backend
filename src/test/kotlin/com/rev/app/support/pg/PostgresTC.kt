package com.rev.app.support.pg

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * JUnit5 @Testcontainers 방식만 사용. 수동 start() 금지.
 * 테스트에서는 Flyway 끄고, init.sql로 스키마 생성.
 */
@Testcontainers
abstract class PostgresTC {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            // ✅ 테스트 리소스에 넣은 스크립트로 스키마 생성
            .withInitScript("db/test/init.sql")

        @JvmStatic
        @DynamicPropertySource
        fun dbProps(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url", postgres::getJdbcUrl)
            reg.add("spring.datasource.username", postgres::getUsername)
            reg.add("spring.datasource.password", postgres::getPassword)

            // 테스트에선 Flyway 비활성 + 하이버네이트가 테이블 생성
            reg.add("spring.flyway.enabled") { false }
            reg.add("spring.jpa.hibernate.ddl-auto") { "update" }

            // 기본 스키마 지정 (엔티티 테이블을 여기로 생성함)
            reg.add("spring.jpa.properties.hibernate.default_schema") { "rev" }

            // 디버깅 옵션
            reg.add("spring.jpa.show-sql") { true }
            reg.add("spring.jpa.properties.hibernate.format_sql") { true }
        }
    }
}

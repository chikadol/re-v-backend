package com.rev.app.config

import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * 기존 H2/로컬 DB에 role 컬럼이 없을 때 자동으로 추가해 주는 보정용 러너.
 * 프로덕션 DB에는 영향이 없으며, 컬럼이 있으면 IF NOT EXISTS로 무시됩니다.
 */
@Component
class SchemaFix(
    private val jdbcTemplate: JdbcTemplate
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        try {
            jdbcTemplate.execute(
                """
                ALTER TABLE rev.users
                ADD COLUMN IF NOT EXISTS role VARCHAR(30) DEFAULT 'USER' NOT NULL;
                """.trimIndent()
            )
            println("✅ schema fix: users.role 컬럼 확인/추가 완료")
        } catch (e: Exception) {
            println("⚠️ schema fix 실패(무시): ${e.message}")
        }

        try {
            jdbcTemplate.execute(
                """
                ALTER TABLE rev.performance
                ADD COLUMN IF NOT EXISTS idol_id UUID;
                """.trimIndent()
            )
            println("✅ schema fix: performance.idol_id 컬럼 확인/추가 완료")
        } catch (e: Exception) {
            println("⚠️ schema fix (performance.idol_id) 실패(무시): ${e.message}")
        }

        try {
            jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS rev.performance_performers (
                    performance_id UUID,
                    performer VARCHAR(100)
                );
                """.trimIndent()
            )
            println("✅ schema fix: performance_performers 테이블 확인/추가 완료")
        } catch (e: Exception) {
            println("⚠️ schema fix (performance_performers) 실패(무시): ${e.message}")
        }

        try {
            jdbcTemplate.execute(
                """
                ALTER TABLE rev.performance
                ADD COLUMN IF NOT EXISTS adv_price INT;
                """.trimIndent()
            )
            jdbcTemplate.execute(
                """
                ALTER TABLE rev.performance
                ADD COLUMN IF NOT EXISTS door_price INT;
                """.trimIndent()
            )
            println("✅ schema fix: performance.adv_price / door_price 컬럼 확인/추가 완료")
        } catch (e: Exception) {
            println("⚠️ schema fix (adv/door price) 실패(무시): ${e.message}")
        }
    }
}


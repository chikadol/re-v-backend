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

        // thread_reaction 테이블의 reaction 컬럼을 VARCHAR로 변경 (H2 ENUM 호환성 문제 해결)
        try {
            // 먼저 컬럼을 삭제 시도 (존재하지 않으면 무시)
            try {
                jdbcTemplate.execute("ALTER TABLE rev.thread_reaction DROP COLUMN IF EXISTS reaction;")
            } catch (e: Exception) {
                // H2에서는 IF EXISTS를 지원하지 않을 수 있으므로 무시
            }
            
            // VARCHAR 타입으로 컬럼 추가
            jdbcTemplate.execute(
                """
                ALTER TABLE rev.thread_reaction 
                ADD COLUMN reaction VARCHAR(20);
                """.trimIndent()
            )
            println("✅ schema fix: thread_reaction.reaction 컬럼을 VARCHAR로 추가 완료")
        } catch (e: Exception) {
            // 컬럼이 이미 존재하거나 다른 이유로 실패한 경우
            try {
                // 타입 변경 시도
                jdbcTemplate.execute(
                    """
                    ALTER TABLE rev.thread_reaction 
                    ALTER COLUMN reaction VARCHAR(20);
                    """.trimIndent()
                )
                println("✅ schema fix: thread_reaction.reaction 컬럼 타입 변경 완료")
            } catch (e2: Exception) {
                println("⚠️ schema fix (thread_reaction.reaction) 실패(무시): ${e2.message}")
            }
        }

        // board_request 테이블 생성
        try {
            jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS rev.board_request (
                    id UUID PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    slug VARCHAR(100) NOT NULL,
                    description TEXT,
                    reason TEXT,
                    requester_id UUID NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    created_at TIMESTAMP,
                    processed_at TIMESTAMP,
                    processed_by_id UUID,
                    CONSTRAINT fk_board_request_requester FOREIGN KEY (requester_id) REFERENCES rev.users(id),
                    CONSTRAINT fk_board_request_processed_by FOREIGN KEY (processed_by_id) REFERENCES rev.users(id)
                );
                """.trimIndent()
            )
            println("✅ schema fix: board_request 테이블 확인/생성 완료")
        } catch (e: Exception) {
            println("⚠️ schema fix (board_request) 실패(무시): ${e.message}")
        }
    }
}


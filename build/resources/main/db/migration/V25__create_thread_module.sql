-- V25__create_thread_module.sql  (PostgreSQL / Flyway)
-- 목적: thread_tag.thread_id 타입을 UUID로 통일하고 FK 정상화

CREATE SCHEMA IF NOT EXISTS rev;

DO $do$
    DECLARE
        v_has_table  boolean;
        v_is_bigint  boolean;
    BEGIN
        SELECT EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema='rev' AND table_name='thread_tag'
        ) INTO v_has_table;

        IF v_has_table THEN
            -- 현재 thread_id의 데이터 타입 확인
            SELECT EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema='rev'
                  AND table_name='thread_tag'
                  AND column_name='thread_id'
                  AND data_type='bigint'
            ) INTO v_is_bigint;

            IF v_is_bigint THEN
                -- bigint -> uuid 안전 변환이 불가하므로 테스트 용도라면 드롭 후 재생성
                -- (운영 데이터가 있다면 마이그레이션 전략 별도 수립 필요)
                DROP TABLE rev.thread_tag CASCADE;
                v_has_table := FALSE; -- 재생성 플래그
            END IF;
        END IF;

        IF NOT v_has_table THEN
            -- UUID 스키마로 정상 생성
            CREATE TABLE IF NOT EXISTS rev.thread_tag (
                                                          thread_id uuid NOT NULL,
                                                          tag       text NOT NULL,
                                                          PRIMARY KEY (thread_id, tag),
                                                          CONSTRAINT fk_thread_tag_thread
                                                              FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE
            );
        ELSE
            -- 이미 존재하고, thread_id가 uuid인 경우: FK가 없으면 보강
            IF NOT EXISTS (
                SELECT 1
                FROM information_schema.table_constraints tc
                WHERE tc.constraint_schema='rev'
                  AND tc.table_name='thread_tag'
                  AND tc.constraint_name='fk_thread_tag_thread'
            ) THEN
                ALTER TABLE rev.thread_tag
                    ADD CONSTRAINT fk_thread_tag_thread
                        FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE;
            END IF;

            -- PK 보강 (없으면 추가)
            IF NOT EXISTS (
                SELECT 1
                FROM information_schema.table_constraints tc
                WHERE tc.constraint_schema='rev'
                  AND tc.table_name='thread_tag'
                  AND tc.constraint_type='PRIMARY KEY'
            ) THEN
                ALTER TABLE rev.thread_tag
                    ADD PRIMARY KEY (thread_id, tag);
            END IF;
        END IF;
    END
$do$;

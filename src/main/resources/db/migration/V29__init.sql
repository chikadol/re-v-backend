-- V29__init.sql
-- 스키마가 필요하면 만들고
CREATE SCHEMA IF NOT EXISTS rev;

-- 간단 예시 테이블들 (실제 엔티티 설계에 맞게 수정!)
CREATE TABLE IF NOT EXISTS rev.board (
                                         id          BIGSERIAL PRIMARY KEY,
                                         name        VARCHAR(255) NOT NULL,
                                         slug        VARCHAR(255) UNIQUE NOT NULL,
                                         description TEXT
);

CREATE TABLE IF NOT EXISTS rev."user" (
                                          id         UUID PRIMARY KEY,
                                          email      VARCHAR(255) UNIQUE NOT NULL
    -- 필요한 컬럼 추가
);

CREATE TABLE IF NOT EXISTS rev.thread (
                                          id           BIGSERIAL PRIMARY KEY,
                                          board_id     BIGINT NOT NULL REFERENCES rev.board(id),
                                          author_id    UUID NOT NULL REFERENCES rev."user"(id),
                                          title        VARCHAR(255) NOT NULL,
                                          content      TEXT NOT NULL,
                                          is_private   BOOLEAN NOT NULL DEFAULT FALSE,
                                          parent_id    BIGINT NULL REFERENCES rev.thread(id),
                                          category_id  UUID NULL,
                                          created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at   TIMESTAMPTZ
);

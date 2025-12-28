-- V6__create_thread.sql
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id BIGSERIAL PRIMARY KEY,
                                          board_id BIGINT NOT NULL,
                                          author_id UUID NOT NULL,
                                          category_id UUID,
                                          title VARCHAR(255) NOT NULL,
                                          content TEXT NOT NULL,
                                          is_private BOOLEAN NOT NULL DEFAULT FALSE,
                                          parent_id BIGINT,
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at TIMESTAMPTZ,
                                          CONSTRAINT thread_author_id_fkey
                                              FOREIGN KEY (author_id) REFERENCES rev.users(id) ON DELETE RESTRICT
);

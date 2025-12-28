-- V7__create_board.sql
CREATE TABLE IF NOT EXISTS rev.board (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL,
                                         slug VARCHAR(100) NOT NULL,
                                         description VARCHAR(255)
);

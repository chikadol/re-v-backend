ALTER TABLE IF EXISTS rev.thread
    DROP CONSTRAINT IF EXISTS thread_author_id_fkey;
ALTER TABLE IF EXISTS rev.thread
    DROP CONSTRAINT IF EXISTS thread_board_id_fkey;

ALTER TABLE IF EXISTS rev.thread
    ADD CONSTRAINT thread_author_id_fkey
        FOREIGN KEY (author_id) REFERENCES rev.users(id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS rev.thread
    ADD CONSTRAINT thread_board_id_fkey
        FOREIGN KEY (board_id) REFERENCES rev.board(id) ON DELETE CASCADE;

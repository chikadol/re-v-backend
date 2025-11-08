-- src/test/resources/test/sql/99_cleanup.sql
-- 테스트 종료 후 데이터만 싹 비우는 용도

SET search_path TO rev, public;

TRUNCATE TABLE
    thread_reaction,
    thread_bookmark,
    thread_tag,
    comment,
    thread,
    board,
    users
    RESTART IDENTITY CASCADE;
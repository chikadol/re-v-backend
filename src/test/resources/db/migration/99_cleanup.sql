-- src/test/resources/test/sql/99_cleanup.sql
truncate table
    rev.thread_bookmark,
    rev.comment,
    rev.thread_tags,
    rev.thread,
    rev.board,
    rev.users
    restart identity cascade;

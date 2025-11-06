TRUNCATE TABLE
    rev.thread_bookmark,
    rev.comment,
    rev.thread
    RESTART IDENTITY CASCADE;

TRUNCATE TABLE
    rev.board,
    rev.users
    RESTART IDENTITY CASCADE;

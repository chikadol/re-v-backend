TRUNCATE TABLE
    rev.thread_reaction,
    rev.thread_bookmark,
    rev.comment,
    rev.thread,         -- ← thread_tags 제거
    rev.board,
    rev.users
    RESTART IDENTITY CASCADE;

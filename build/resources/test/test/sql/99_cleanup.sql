-- src/test/resources/test/sql/99_cleanup.sql

SET search_path TO rev, public;

DO $do$
    BEGIN
        IF to_regclass('rev.thread_reaction') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.thread_reaction RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.thread_bookmark') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.thread_bookmark RESTART IDENTITY CASCADE';
        END IF;

        -- 프로젝트 실제 테이블명에 맞추세요. 둘 중 하나만 있을 수 있습니다.
        IF to_regclass('rev.thread_tag') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.thread_tag RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.thread_tags') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.thread_tags RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.comment') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.comment RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.thread') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.thread RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.board') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.board RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.users') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.users RESTART IDENTITY CASCADE';
        END IF;
    END;
$do$;
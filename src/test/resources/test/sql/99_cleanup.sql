DO $do$
    BEGIN
        IF to_regclass('rev.thread') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.thread RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.board') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.board RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev.users') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev.users RESTART IDENTITY CASCADE';
        END IF;

        IF to_regclass('rev."user"') IS NOT NULL THEN
            EXECUTE 'TRUNCATE TABLE rev."user" RESTART IDENTITY CASCADE';
        END IF;
    END;
$do$;

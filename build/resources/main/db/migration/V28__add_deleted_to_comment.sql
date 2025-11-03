ALTER TABLE rev.comment
    ADD COLUMN IF NOT EXISTS deleted boolean NOT NULL DEFAULT false;

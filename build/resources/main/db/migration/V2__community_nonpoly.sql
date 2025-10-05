-- V2__community_nonpoly.sql
SET search_path TO rev, public;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'reaction_kind') THEN
        CREATE TYPE reaction_kind AS ENUM('LIKE','DISLIKE');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'moderation_action') THEN
        CREATE TYPE moderation_action AS ENUM('PIN','UNPIN','HIDE','DELETE','RESTORE');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'report_status') THEN
        CREATE TYPE report_status AS ENUM('OPEN','REVIEWING','RESOLVED','REJECTED');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS board (
  id BIGSERIAL PRIMARY KEY,
  slug TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  is_anonymous_allowed BOOLEAN DEFAULT TRUE,
  is_private BOOLEAN DEFAULT FALSE,
  rules JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS thread (
  id BIGSERIAL PRIMARY KEY,
  board_id BIGINT NOT NULL REFERENCES board(id) ON DELETE CASCADE,
  author_id BIGINT NOT NULL REFERENCES "user"(id),
  title TEXT NOT NULL,
  content TEXT,
  is_anonymous BOOLEAN DEFAULT FALSE,
  display_no BIGINT NOT NULL,
  view_count BIGINT DEFAULT 0,
  like_count INT DEFAULT 0,
  dislike_count INT DEFAULT 0,
  comment_count INT DEFAULT 0,
  pinned_until TIMESTAMPTZ,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_thread_board_display ON thread(board_id, display_no);

CREATE TABLE IF NOT EXISTS thread_attachment (
  id BIGSERIAL PRIMARY KEY,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  type TEXT NOT NULL,
  path TEXT NOT NULL,
  width INT,
  height INT,
  duration INT,
  order_no INT DEFAULT 0,
  metadata JSONB DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS comment (
  id BIGSERIAL PRIMARY KEY,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  parent_id BIGINT REFERENCES comment(id) ON DELETE CASCADE,
  author_id BIGINT NOT NULL REFERENCES "user"(id),
  content TEXT NOT NULL,
  is_anonymous BOOLEAN DEFAULT FALSE,
  like_count INT DEFAULT 0,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bookmark (
  user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY(user_id, thread_id)
);

CREATE TABLE IF NOT EXISTS thread_reaction (
  id BIGSERIAL PRIMARY KEY,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  kind reaction_kind NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(user_id, thread_id, kind)
);

CREATE TABLE IF NOT EXISTS comment_reaction (
  id BIGSERIAL PRIMARY KEY,
  comment_id BIGINT NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  kind reaction_kind NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(user_id, comment_id, kind)
);

CREATE TABLE IF NOT EXISTS thread_report (
  id BIGSERIAL PRIMARY KEY,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  reporter_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  status report_status DEFAULT 'OPEN',
  reason TEXT,
  detail TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  resolved_by BIGINT REFERENCES "user"(id),
  resolved_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS comment_report (
  id BIGSERIAL PRIMARY KEY,
  comment_id BIGINT NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
  reporter_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  status report_status DEFAULT 'OPEN',
  reason TEXT,
  detail TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  resolved_by BIGINT REFERENCES "user"(id),
  resolved_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS board_manager (
  board_id BIGINT NOT NULL REFERENCES board(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  PRIMARY KEY(board_id, user_id)
);

CREATE TABLE IF NOT EXISTS view_log_daily (
  date DATE NOT NULL,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  views BIGINT DEFAULT 0,
  PRIMARY KEY(date, thread_id)
);

CREATE TABLE IF NOT EXISTS thread_moderation_log (
  id BIGSERIAL PRIMARY KEY,
  thread_id BIGINT NOT NULL REFERENCES thread(id) ON DELETE CASCADE,
  actor_user_id BIGINT NOT NULL REFERENCES "user"(id),
  action moderation_action NOT NULL,
  reason TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS comment_moderation_log (
  id BIGSERIAL PRIMARY KEY,
  comment_id BIGINT NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
  actor_user_id BIGINT NOT NULL REFERENCES "user"(id),
  action moderation_action NOT NULL,
  reason TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- 성능 최적화를 위한 인덱스 추가

-- Thread 테이블 인덱스
-- 게시판별 게시글 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_board_id_is_private ON rev.thread(board_id, is_private);

-- 작성자별 게시글 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_author_id ON rev.thread(author_id);

-- 생성일시 정렬 최적화
CREATE INDEX IF NOT EXISTS idx_thread_created_at ON rev.thread(created_at DESC);

-- 제목 검색 최적화 (LIKE 검색)
CREATE INDEX IF NOT EXISTS idx_thread_title ON rev.thread(title);

-- ThreadTag 테이블 인덱스
-- 태그별 게시글 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_tag_tag_id ON rev.thread_tag(tag_id);

-- 게시글별 태그 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_tag_thread_id ON rev.thread_tag(thread_id);

-- Comment 테이블 인덱스
-- 게시글별 댓글 조회 최적화
CREATE INDEX IF NOT EXISTS idx_comment_thread_id ON rev.comment(thread_id);

-- 작성자별 댓글 조회 최적화
CREATE INDEX IF NOT EXISTS idx_comment_author_id ON rev.comment(author_id);

-- ThreadBookmark 테이블 인덱스
-- 사용자별 북마크 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_bookmark_user_id ON rev.thread_bookmark(user_id);

-- 게시글별 북마크 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_bookmark_thread_id ON rev.thread_bookmark(thread_id);

-- ThreadReaction 테이블 인덱스
-- 게시글별 반응 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_reaction_thread_id ON rev.thread_reaction(thread_id);

-- 사용자별 반응 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_reaction_user_id ON rev.thread_reaction(user_id);

-- 게시글 + 사용자 + 타입 조회 최적화
CREATE INDEX IF NOT EXISTS idx_thread_reaction_thread_user_type ON rev.thread_reaction(thread_id, user_id, type);

-- Notification 테이블 인덱스
-- 사용자별 알림 조회 최적화
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON rev.notification(user_id);

-- 읽음 상태별 조회 최적화
CREATE INDEX IF NOT EXISTS idx_notification_user_id_is_read ON rev.notification(user_id, is_read);

-- 생성일시 정렬 최적화
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON rev.notification(created_at DESC);


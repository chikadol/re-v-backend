-- notification 테이블 컬럼명을 엔티티와 일치하도록 수정
ALTER TABLE IF EXISTS rev.notification 
    RENAME COLUMN user_id TO receiver_id;

ALTER TABLE IF EXISTS rev.notification 
    RENAME COLUMN ref_thread_id TO thread_id;

ALTER TABLE IF EXISTS rev.notification 
    RENAME COLUMN ref_comment_id TO comment_id;

ALTER TABLE IF EXISTS rev.notification 
    RENAME COLUMN content TO message;

-- 인덱스도 재생성
DROP INDEX IF EXISTS rev.ix_notification_user;
CREATE INDEX IF NOT EXISTS ix_notification_user ON rev.notification(receiver_id, is_read);


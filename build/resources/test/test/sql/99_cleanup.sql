-- 포터블 정리 스크립트: FK 의존 역순으로 DROP (다 존재할 수도/없을 수도 있으니 IF EXISTS)
DROP TABLE IF EXISTS rev.thread_reaction;
DROP TABLE IF EXISTS rev.thread_bookmark;
DROP TABLE IF EXISTS rev.thread_tags;
DROP TABLE IF EXISTS rev.comment;
DROP TABLE IF EXISTS rev.thread;
DROP TABLE IF EXISTS rev.board;
-- users 는 다음 테스트에서도 재사용 가능하면 남겨도 됨. 완전 초기화가 필요하면 아래 주석 해제
-- DROP TABLE IF EXISTS rev.users;

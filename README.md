# RE-V Backend — 실전 삽질 기록과 설정 가이드

## 우리가 겪었던 주요 이슈와 해결 여정

- **Gradle 의존성 지옥**: 중복/버전 충돌을 정리하고 `springdoc`, `jjwt`, `mockito` 등을 단일 버전으로 통일.
- **OAuth2 (구글/네이버/카카오)**  
  - 빈 클라이언트 ID로 인한 `Client id must not be empty` 예외 → `OAuth2ClientCondition`으로 환경변수 존재 시에만 빈 생성.  
  - `No static resource oauth2/authorization/*` → Security 체인에서 OAuth2 세션 허용 + `OAuth2SuccessHandler` 직접 리다이렉트.  
  - 로그인 후 콜백 리다이렉트 누락 → 절대 URL 리다이렉트로 고정, `/auth/callback` 백업 엔드포인트 정리.
- **DB 연결/마이그레이션**: H2 URL에 `INIT` 구문으로 인한 SQL 오류 제거, Flyway 스키마 충돌 수정.
- **알림 기능**: 댓글 작성 시 쓰레드 작성자에게 알림 생성(익명 게시판 대응, 메시지 익명 처리).
- **크롤러 성능**: 겐바 크롤러에 `fast` 플래그 추가(상세 가격 추출 스킵)로 속도 개선.
- **프론트/백엔드 연동**: Vite 프록시가 `/auth/callback`을 백엔드로 우회시키는 문제 → `/auth/login|register|refresh`만 프록시하도록 수정.
- **UI 개선**: 메인 랜딩 페이지 추가, 헤더 로고 클릭 시 홈으로 이동, 공연 목록이 비면 전체 목록 재조회.

## 실행 방법

### 사전 준비
- JDK 21
- (선택) Docker로 Postgres/H2 대체 사용 가능

### macOS / Linux
```bash
# 1) (옵션) DB 띄우기
docker compose up -d

# 2) 환경변수 로드 후 서버 실행 (권장)
./start-with-env.sh

# 또는 수동 로드
source load-env.sh && ./gradlew bootRun
```

### Windows (PowerShell)
```powershell
# 1) (옵션) DB 띄우기
docker compose up -d

# 2) 서버 실행
.\gradlew.bat bootRun
```

엔드포인트
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health:     http://localhost:8080/health

## 환경 변수(.env) 핵심
`.env.example`을 `.env`로 복사해 값만 채우면 됩니다.
- DB: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- JWT: `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION`
- OAuth2(선택): `GOOGLE_CLIENT_ID/SECRET`, `NAVER_CLIENT_ID/SECRET`, `KAKAO_CLIENT_ID/SECRET`
  - 환경변수가 없으면 OAuth2는 자동 비활성화.
  - 상세 가이드: [README_OAUTH2.md](./README_OAUTH2.md)
  - 설정 스크립트: `./setup-oauth.sh` (macOS/Linux), `.\setup-oauth.ps1` (Windows)

## 주요 기능 정리
- **인증**: 이메일/비밀번호 + OAuth2(구글/네이버/카카오), JWT 발급·갱신
- **알림**: 댓글 생성 시 쓰레드 작성자에게 알림 (익명 메시지)
- **게시판/쓰레드/댓글**: 작성자 식별자 마스킹(익명 게시판 느낌)
- **공연/티켓**: 공연 조회, 상태(UPCOMING/ONGOING/ENDED), 티켓 구매
- **크롤러**: 겐바/아지토 일정 크롤링, `fast` 모드로 속도 개선

## 프론트엔드 연동
```bash
cd ../rev-frontend
npm install
npm run dev   # http://localhost:5173
```
- Vite 프록시: `/auth/login|register|refresh`, `/api`만 백엔드로 전달. `/auth/callback`은 프론트 라우터가 처리.

## 문제 해결 체크리스트
- OAuth2 콜백 404: 프론트 라우터(`/auth/callback`)가 처리하도록 Vite 프록시 확인.
- OAuth2 빈 클라이언트 오류: 환경변수 설정 여부 확인(없으면 자동 비활성).
- 크롤링이 느릴 때: `fast=true`로 호출(`/api/admin/agito-crawler/crawl?fast=true`).
- 공연 목록이 비면: 프론트에서 전체 목록을 한 번 더 조회하도록 처리되어 있음. 그래도 없으면 `POST /api/performances/test-data`로 샘플 생성.

## 우리가 배운 점
- 조건부 빈 생성(@Conditional)과 커스텀 Condition으로 OAuth2를 “환경변수 존재 시”만 켜기.
- Security 체인에서 OAuth2 구간만 세션 허용, 나머지는 JWT Stateless.
- 프런트 프록시 설정 한 줄이 OAuth2 콜백을 404로 만들 수 있다 → 콜백 경로는 프런트에서만 처리.
- 크롤러는 “상세 추출은 옵션화, 기본은 빠르게”가 체감 품질을 높인다.

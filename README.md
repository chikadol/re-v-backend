# RE-V Backend — 실전 개발 기록과 완전한 설정 가이드

> RE-V는 지하 아이돌 공연 정보와 커뮤니티를 연결하는 플랫폼입니다.  
> 이 문서는 프로젝트 개발 과정에서 겪은 모든 시행착오와 해결 방법을 기록한 실전 개발 가이드입니다.

---

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [환경 변수 설정](#환경-변수-설정)
- [API 엔드포인트](#api-엔드포인트)
- [개발 과정에서 겪은 주요 이슈와 해결](#개발-과정에서-겪은-주요-이슈와-해결)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [프론트엔드 연동](#프론트엔드-연동)
- [문제 해결 체크리스트](#문제-해결-체크리스트)
- [우리가 배운 점](#우리가-배운-점)

---

## 프로젝트 개요

RE-V는 지하 아이돌 공연 정보를 크롤링하고, 커뮤니티 기능을 제공하는 통합 플랫폼입니다.

핵심 가치:
- 🎭 공연 정보 통합: 여러 사이트에서 공연 일정을 자동으로 수집
- 💬 익명 커뮤니티: 익명 게시판으로 자유로운 소통
- 🎫 티켓 관리: 공연 티켓 예매 및 관리
- 🔔 실시간 알림: 댓글, 반응 등 활동 알림
- 👥 역할 기반 접근 제어: 일반 유저, 아이돌, 관리자 역할 분리

---

## 주요 기능

### 🔐 인증 및 권한 관리

- 이메일/비밀번호 로그인: JWT 기반 인증
- 소셜 로그인: Google, Naver, Kakao OAuth2 지원
- 역할 기반 접근 제어 (RBAC):
  - `USER`: 일반 사용자
  - `IDOL`: 지하 아이돌 (공연 등록, 아이돌 정보 등록 가능)
  - `ADMIN`: 관리자 (게시판 생성, 게시판 생성 요청 승인/거부)
- JWT 토큰: Access Token + Refresh Token
- 토큰 갱신: Refresh Token을 통한 자동 갱신

### 📝 커뮤니티 기능

- 게시판 관리:
  - 게시판 목록 조회
  - 게시판 생성 (관리자 전용)
  - 게시판 생성 요청 (일반 유저 → 관리자 승인)
- 게시글 (Thread):
  - 게시글 작성, 수정, 삭제
  - 태그 시스템
  - 익명 처리 (작성자 ID 마스킹)
  - 검색 기능 (제목, 내용, 댓글 내용 검색)
  - 태그 필터링
  - 페이지네이션
- 댓글 (Comment):
  - 댓글 작성, 대댓글 지원
  - 익명 처리
  - 게시글 작성자가 자신의 게시글에 댓글을 달면 "글쓴 익명"으로 표시
  - 댓글 작성 시 게시글 작성자에게 알림
- 반응 (Reaction):
  - LIKE, LOVE, LAUGH, SAD, ANGRY 반응
  - 반응 토글 기능
- 북마크: 게시글 북마크 저장 및 조회

### 🎭 공연 및 티켓 관리

- 공연 정보:
  - 공연 목록 조회 (전체, 예정 공연)
  - 공연 상세 정보
  - 아이돌별 공연 조회
  - 출연진 정보 표시
  - 사전예매 가격 / 현장예매 가격 구분
- 공연 등록 (IDOL 권한):
  - 공연 제목, 설명, 장소, 일시
  - 출연진 정보
  - 사전예매 가격, 현장예매 가격
  - 총 좌석 수
- 크롤러:
  - Genba 크롤러: 공연 일정 자동 수집
  - Fast 모드: 상세 가격 추출 스킵으로 속도 개선
  - 출연진 정보 자동 추출 및 아이돌 엔티티 자동 생성/연결

### 👤 사용자 기능

- 내 정보 페이지:
  - 요약: 작성한 글, 댓글, 북마크, 읽지 않은 알림 개수
  - 내 글: 작성한 게시글 목록
  - 북마크: 북마크한 게시글 목록
  - 내 댓글: 작성한 댓글 목록
  - 각 카드 클릭 시 해당 목록으로 이동
- 알림 시스템:
  - 댓글 작성 시 게시글 작성자에게 알림
  - 읽음/읽지 않음 상태 관리
  - 읽지 않은 알림 개수 조회

### 🌟 아이돌 관리 (IDOL 권한)

- 아이돌 정보:
  - 아이돌 목록 조회
  - 아이돌 상세 정보
  - 아이돌 등록 (IDOL 권한)
  - 아이돌 삭제 (IDOL 권한)
  - 아이돌별 공연 목록 조회

### 🔍 검색 기능

- 게시판 내 검색:
  - 제목 검색
  - 내용 검색
  - 댓글 내용 검색
  - 대소문자 구분 없음
  - 부분 일치 검색

---

## 기술 스택

### Backend
- 언어: Kotlin
- 프레임워크: Spring Boot 3.x
- 데이터베이스: PostgreSQL (프로덕션), H2 (로컬 개발)
- ORM: JPA / Hibernate
- 마이그레이션: Flyway
- 인증: Spring Security + JWT
- OAuth2: Spring Security OAuth2 Client
- API 문서: Swagger (OpenAPI 3)
- 빌드 도구: Gradle (Kotlin DSL)
- 웹 크롤링: JSoup, Selenium WebDriver

### Frontend
- 언어: TypeScript
- 프레임워크: React 18
- 라우팅: React Router
- 빌드 도구: Vite
- 스타일링: CSS Variables (커스텀 디자인 시스템)

---

## 시작하기

### 사전 요구사항

- JDK 21 이상
- Docker (선택사항, PostgreSQL 사용 시)
- Node.js 18+ (프론트엔드 개발 시)

### 빠른 시작

#### 1. 저장소 클론
```bash
git clone <repository-url>
cd re-v-backend
```

#### 2. 환경 변수 설정
```bash
# .env.example을 .env로 복사
cp .env.example .env

# 환경 변수 편집
# 최소한 DATABASE_URL, JWT_SECRET은 설정 필요
```

#### 3. 데이터베이스 설정

옵션 A: Docker Compose 사용 (권장)
```bash
docker compose up -d
```

옵션 B: 로컬 PostgreSQL 사용
- PostgreSQL 설치 및 실행
- `revdb` 데이터베이스 생성
- `.env` 파일에 연결 정보 설정

옵션 C: H2 인메모리 DB (개발용)
- `.env`에서 `DATABASE_URL`을 H2로 설정
- 별도 설치 불필요

#### 4. 서버 실행

macOS / Linux:
```bash
# 환경 변수 자동 로드 후 실행 (권장)
./start-with-env.sh

# 또는 수동 로드
source load-env.sh && ./gradlew bootRun
```

Windows (PowerShell):
```powershell
.\gradlew.bat bootRun
```

#### 5. 확인
- 서버 실행 확인: http://localhost:8080/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html

---

## 환경 변수 설정

### 필수 환경 변수

`.env` 파일에 다음 변수들을 설정해야 합니다:

```bash
# 데이터베이스
DATABASE_URL=jdbc:postgresql://localhost:5432/revdb
DATABASE_USERNAME=rev
DATABASE_PASSWORD=revpass

# JWT
JWT_SECRET=your-secret-key-min-32-characters-long
JWT_ACCESS_TOKEN_EXPIRATION=3600000  # 1시간 (밀리초)
JWT_REFRESH_TOKEN_EXPIRATION=604800000  # 7일 (밀리초)
```

### 선택적 환경 변수 (OAuth2)

소셜 로그인을 사용하려면 다음 변수들을 설정하세요:

```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Naver OAuth2
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

참고: OAuth2 환경 변수가 없으면 소셜 로그인은 자동으로 비활성화됩니다.

### OAuth2 설정 가이드

상세한 OAuth2 설정 방법은 [README_OAUTH2.md](./README_OAUTH2.md)를 참조하세요.

빠른 설정 스크립트:
```bash
# macOS / Linux
./setup-oauth.sh

# Windows (PowerShell)
.\setup-oauth.ps1
```

---

## API 엔드포인트

### 인증

- `POST /auth/register` - 회원가입 (역할 선택 가능: USER, IDOL)
- `POST /auth/login` - 로그인
- `POST /auth/refresh` - 토큰 갱신
- `GET /oauth2/authorization/{provider}` - 소셜 로그인 (google, naver, kakao)

### 게시판

- `GET /api/boards` - 게시판 목록
- `GET /api/boards/{id}` - 게시판 상세
- `POST /api/boards` - 게시판 생성 (ADMIN 전용)
- `GET /api/board-requests` - 내 게시판 생성 요청 목록
- `POST /api/board-requests` - 게시판 생성 요청 제출
- `GET /api/board-requests/pending` - 대기 중인 요청 목록 (ADMIN 전용)
- `POST /api/board-requests/{id}/process` - 요청 승인/거부 (ADMIN 전용)

### 게시글

- `GET /api/threads/{boardId}/threads` - 게시글 목록 (검색, 태그 필터 지원)
- `GET /api/threads/{threadId}` - 게시글 상세
- `POST /api/threads/{boardId}/threads` - 게시글 작성
- `POST /api/threads/{threadId}/reactions/{type}` - 반응 토글

### 댓글

- `GET /api/comments/threads/{threadId}` - 댓글 목록
- `POST /api/comments` - 댓글 작성

### 공연

- `GET /api/performances` - 공연 목록
- `GET /api/performances/upcoming` - 예정 공연 목록
- `GET /api/performances/{id}` - 공연 상세
- `POST /api/performances` - 공연 등록 (IDOL 전용)
- `GET /api/idols/{idolId}/performances` - 아이돌별 공연 목록

### 아이돌

- `GET /api/idols` - 아이돌 목록
- `GET /api/idols/{id}` - 아이돌 상세
- `POST /api/idols` - 아이돌 등록 (IDOL 전용)
- `DELETE /api/idols/{id}` - 아이돌 삭제 (IDOL 전용)

### 내 정보

- `GET /api/me/overview` - 내 정보 요약
- `GET /api/me/threads` - 내가 작성한 글 목록
- `GET /api/me/bookmarks/threads` - 내 북마크 목록
- `GET /api/me/comments` - 내가 작성한 댓글 목록

### 알림

- `GET /api/notifications` - 알림 목록
- `POST /api/notifications/{id}/read` - 알림 읽음 처리
- `POST /api/notifications/read-all` - 모든 알림 읽음 처리
- `GET /api/notifications/unread-count` - 읽지 않은 알림 개수

### 관리자

- `GET /api/admin/agito-crawler/crawl` - 크롤링 실행 (fast 모드 지원)
- `DELETE /api/admin/idols/all` - 모든 아이돌 삭제

---

## 개발 과정에서 겪은 주요 이슈와 해결

### 1. Gradle 의존성 지옥

문제: 중복된 의존성 선언, 버전 충돌로 인한 빌드 실패

해결:
- `build.gradle.kts`에서 중복 의존성 제거
- 주요 라이브러리 버전 통일:
  - `springdoc-openapi-starter-webmvc-ui: 2.6.0`
  - `postgresql: 42.7.4`
  - `mockito-kotlin: 5.3.1`
- `exclude`를 사용하여 불필요한 의존성 제거

교훈: 의존성 관리는 프로젝트 초기부터 체계적으로 해야 한다.

---

### 2. OAuth2 소셜 로그인 통합의 시행착오

#### 2.1. 빈 클라이언트 ID 오류

문제: `Client id must not be empty` 예외 발생

원인: Spring Boot의 `OAuth2ClientAutoConfiguration`이 환경 변수가 없어도 빈 클라이언트 ID를 검증하려고 시도

해결:
1. `@SpringBootApplication`에서 `OAuth2ClientAutoConfiguration` 제외
2. 커스텀 `OAuth2ClientCondition` 생성하여 환경 변수 존재 시에만 빈 생성
3. `NonValidatingOAuth2ClientProperties` 빈 제공하여 기본 검증 우회

코드:
```kotlin
@Conditional(OAuth2ClientCondition::class)
fun clientRegistrationRepository(): ClientRegistrationRepository {
    // 환경 변수 확인 후 빈 생성
}
```

#### 2.2. OAuth2 리다이렉트 경로 404

문제: `No static resource oauth2/authorization/google` 오류

원인: Security 체인에서 OAuth2 필터가 제대로 등록되지 않음

해결:
1. OAuth2 경로에 대해 세션 관리 활성화 (`SessionCreationPolicy.IF_REQUIRED`)
2. `OAuth2SuccessHandler`에서 명시적 리다이렉트 처리
3. 프론트엔드 Vite 프록시에서 `/auth/callback` 제외

#### 2.3. 무한 리다이렉트 루프

문제: `ERR_TOO_MANY_REDIRECTS` 발생

원인: 백엔드와 프론트엔드 모두에서 리다이렉트 처리 시도

해결:
- 백엔드의 `/auth/callback` 엔드포인트 제거
- `OAuth2SuccessHandler`에서만 프론트엔드로 리다이렉트
- Vite 프록시에서 `/auth/callback`을 프론트엔드 라우터가 처리하도록 설정

교훈: OAuth2는 세션 기반이므로 JWT와 혼용 시 주의가 필요하다. 경로별로 세션 정책을 다르게 설정해야 한다.

---

### 3. 데이터베이스 스키마 관리

#### 3.1. H2와 PostgreSQL의 ENUM 타입 차이

문제: `Value not permitted for column "('DISLIKE', 'LIKE', 'WOW')": "LOVE"` 오류

원인:
- H2는 ENUM 타입을 다르게 처리
- Flyway 마이그레이션에서 정의한 ENUM 값과 실제 사용하는 값이 불일치
- H2가 초기 데이터를 기반으로 ENUM을 생성하여 'LOVE'가 포함되지 않음

해결:
- `SchemaFix`에서 `thread_reaction.reaction` 컬럼을 `VARCHAR(20)`으로 강제 변경
- H2의 ENUM 동작을 우회하여 문자열로 저장

코드:
```kotlin
jdbcTemplate.execute("""
    ALTER TABLE rev.thread_reaction 
    ALTER COLUMN reaction SET DATA TYPE VARCHAR(20);
""")
```

#### 3.2. 동적 스키마 수정

문제: 개발 중 스키마 변경이 빈번하여 Flyway 마이그레이션만으로는 부족

해결:
- `SchemaFix` `CommandLineRunner` 생성
- 애플리케이션 시작 시 자동으로 누락된 컬럼/테이블 추가
- `IF NOT EXISTS` 패턴으로 안전하게 처리

추가된 스키마 수정:
- `users.role` 컬럼 추가
- `performance.idol_id` 컬럼 추가
- `performance_performers` 테이블 생성
- `performance.adv_price`, `door_price` 컬럼 추가
- `board_request` 테이블 생성

교훈: 개발 환경과 프로덕션 환경의 DB 차이를 고려한 유연한 스키마 관리가 필요하다.

---

### 4. 반응(Reaction) 기능 구현의 난관

#### 4.1. UUID와 Long 타입 불일치

문제: `For input string: "83311f0d-6883-4278-9d12-b247d1f2260f"` 오류

원인: Spring이 `threadId` (UUID)를 `Long`으로 자동 변환 시도

해결:
- `@PathVariable`에서 `String`으로 받아서 수동으로 `UUID` 변환
- 명시적 타입 변환으로 안전성 확보

#### 4.2. ID 타입 불일치 (UUID vs BIGINT)

문제: `Data conversion error converting "'93f3267e-1c33-41b6-a393-9de20a47beb5' (THREAD_REACTION: ""ID"" BIGINT...)`

원인: 
- `ThreadReactionEntity`의 `id`가 `UUID`로 정의되어 있었으나
- H2 데이터베이스는 `BIGINT`로 자동 생성

해결:
- `ThreadReactionEntity.id`를 `Long`으로 변경
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` 사용
- `ThreadReactionRepository`의 제네릭 타입도 `Long`으로 변경

#### 4.3. NULL 제약 조건 위반

문제: `NULL not allowed for column "REACTION"`

원인: H2가 자동 생성한 `reaction` 컬럼이 `NOT NULL`이었으나 엔티티에서 매핑하지 않음

해결:
- `ThreadReactionEntity`에 `reaction: String` 필드 추가
- `@PrePersist`, `@PreUpdate`로 `type`과 `reaction` 동기화

코드:
```kotlin
@Column(name = "reaction", nullable = false, length = 20)
var reaction: String = ""
    get() = type

@PrePersist
@PreUpdate
fun syncReactionBeforeSave() {
    reaction = type
}
```

교훈: 데이터베이스 스키마와 엔티티 매핑을 정확히 일치시켜야 한다. 특히 H2와 PostgreSQL의 차이를 고려해야 한다.

---

### 5. 크롤러 성능 최적화

문제: 크롤링 속도가 너무 느림 (각 공연의 상세 페이지를 Selenium으로 접근)

해결:
- `fast` 모드 추가: 상세 가격 추출 스킵
- 출연진 정보는 JSoup으로 빠르게 추출
- 비동기 처리로 즉시 응답 반환

성능 개선:
- 일반 모드: 공연당 약 3-5초
- Fast 모드: 공연당 약 0.5-1초

교훈: 사용자 경험을 위해 선택적 상세 정보 추출이 중요하다.

---

### 6. 익명 게시판 구현

요구사항: 모든 게시글과 댓글에서 작성자 정보를 숨기되, 게시글 작성자가 자신의 게시글에 댓글을 달면 "글쓴 익명"으로 표시

구현:
1. `ThreadRes`, `CommentRes`에서 `authorId`를 항상 `null`로 반환
2. `CommentRes`에 `isAuthor` 필드 추가
3. 댓글 매퍼에서 게시글 작성자와 댓글 작성자 비교
4. 프론트엔드에서 `isAuthor`가 `true`이면 "글쓴 익명" 표시

코드:
```kotlin
fun CommentEntity.toRes(): CommentRes {
    val threadAuthorId = thread?.author?.id
    val commentAuthorId = author?.id
    val isAuthor = threadAuthorId != null && 
                   commentAuthorId != null && 
                   threadAuthorId == commentAuthorId
    
    return CommentRes(
        // ...
        isAuthor = isAuthor
    )
}
```

교훈: 비즈니스 로직을 DTO 매핑 단계에서 처리하면 클라이언트 코드가 간결해진다.

---

### 7. 역할 기반 접근 제어 (RBAC) 구현

요구사항:
- 일반 유저: 기본 기능 사용
- 아이돌: 공연 등록, 아이돌 정보 등록 가능
- 관리자: 게시판 생성, 게시판 생성 요청 승인/거부

구현:
1. `UserRole` enum 생성 (USER, IDOL, ADMIN)
2. `UserEntity`에 `role` 컬럼 추가
3. JWT 토큰에 `roles` 클레임 포함
4. `@PreAuthorize("hasRole('ADMIN')")` 사용
5. 프론트엔드에서 JWT 디코딩하여 역할 확인

JWT에 roles 포함:
```kotlin
fun generateAccessToken(userId: UUID, roles: List<String>): String {
    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("roles", roles) // roles 클레임 추가
        // ...
}
```

프론트엔드 역할 확인:
```typescript
export function getUserRole(): string | null {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;
  
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.roles?.[0] || null;
}
```

교훈: JWT에 역할 정보를 포함하면 프론트엔드에서도 권한 기반 UI를 쉽게 구현할 수 있다.

---

### 8. 게시판 생성 요청 시스템

요구사항: 일반 유저는 게시판을 직접 생성할 수 없고, 요청을 제출하여 관리자가 승인해야 함

구현:
1. `BoardRequestEntity` 생성 (상태: PENDING, APPROVED, REJECTED)
2. 일반 유저: `/api/board-requests` POST로 요청 제출
3. 관리자: `/api/board-requests/pending` GET으로 대기 목록 조회
4. 관리자: `/api/board-requests/{id}/process` POST로 승인/거부
5. 승인 시 자동으로 게시판 생성

프론트엔드:
- 일반 유저: "게시판 생성 요청" 버튼 → `RequestBoardPage`
- 관리자: "게시판 생성" 버튼 → `CreateBoardPage`

교훈: 승인 워크플로우는 별도의 엔티티와 상태 관리가 필요하다.

---

### 9. 검색 기능 구현

요구사항: 게시판에서 제목, 내용, 댓글 내용으로 검색

구현:
1. `ThreadRepository`에 검색 쿼리 추가
2. `CommentEntity`를 LEFT JOIN하여 댓글 내용도 검색 대상에 포함
3. `DISTINCT` 사용하여 중복 제거
4. 대소문자 구분 없이 검색 (`LOWER()` 함수 사용)

쿼리:
```sql
select distinct th from ThreadEntity th
join th.board b
left join CommentEntity c on c.thread = th
where b.id = :boardId 
  and th.isPrivate = false
  and (lower(th.title) like lower(concat('%', :keyword, '%'))
       or lower(th.content) like lower(concat('%', :keyword, '%'))
       or lower(c.content) like lower(concat('%', :keyword, '%')))
```

교훈: JOIN을 활용하면 관련 엔티티의 데이터도 검색 대상에 포함시킬 수 있다.

---

### 10. 내 정보 페이지 개선

요구사항: 요약 카드의 숫자를 클릭하면 해당 목록으로 이동

구현:
1. 각 카드를 버튼으로 변경
2. 클릭 시 해당 탭으로 전환 또는 페이지 이동
3. "내 글" 탭 추가 (`/api/me/threads`)

기능:
- 작성한 글 클릭 → "내 글" 탭
- 작성한 댓글 클릭 → "내 댓글" 탭
- 북마크 클릭 → "북마크" 탭
- 읽지 않은 알림 클릭 → 알림 페이지

교훈: 사용자 경험을 위해 인터랙티브한 UI 요소가 중요하다.

---

## 데이터베이스 스키마

### 주요 테이블

- users: 사용자 정보 (역할 포함)
- board: 게시판
- board_request: 게시판 생성 요청
- thread: 게시글
- comment: 댓글
- thread_reaction: 게시글 반응
- thread_bookmark: 게시글 북마크
- notification: 알림
- performance: 공연 정보
- idol: 아이돌 정보
- performance_performers: 공연 출연진 (ElementCollection)

### 스키마 자동 수정

`SchemaFix` 컴포넌트가 애플리케이션 시작 시 자동으로:
- 누락된 컬럼 추가
- 누락된 테이블 생성
- 타입 불일치 수정

주의: 프로덕션 환경에서는 Flyway 마이그레이션을 사용하고, `SchemaFix`는 개발 환경에서만 사용하는 것을 권장합니다.

---

## 프론트엔드 연동

### Vite 프록시 설정

`rev-frontend/vite.config.ts`:
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
  '/auth/login': { target: 'http://localhost:8080', changeOrigin: true },
  '/auth/register': { target: 'http://localhost:8080', changeOrigin: true },
  '/auth/refresh': { target: 'http://localhost:8080', changeOrigin: true },
  // /auth/callback은 프론트엔드 라우터가 처리
}
```

### CORS 설정

백엔드 `SecurityConfig`에서 다음 origins 허용:
- `http://localhost:*`
- `http://127.0.0.1:*`

---

## 문제 해결 체크리스트

### OAuth2 관련

- [ ] 콜백 404 오류: Vite 프록시에서 `/auth/callback` 제외 확인
- [ ] 빈 클라이언트 오류: 환경 변수 설정 여부 확인 (없으면 자동 비활성화)
- [ ] 무한 리다이렉트: 백엔드 `/auth/callback` 엔드포인트 제거 확인

### 데이터베이스 관련

- [ ] 컬럼 없음 오류: `SchemaFix` 로그 확인, 필요시 수동으로 컬럼 추가
- [ ] ENUM 타입 오류: H2 사용 시 `VARCHAR`로 변경되었는지 확인
- [ ] 연결 타임아웃: Docker Compose가 실행 중인지 확인

### 인증 관련

- [ ] JWT 토큰 만료: Refresh Token으로 갱신
- [ ] 역할 확인 실패: JWT 토큰에 `roles` 클레임이 포함되어 있는지 확인

### 크롤러 관련

- [ ] 크롤링이 느릴 때: `fast=true` 파라미터 사용
- [ ] 공연 목록이 비어있을 때: 크롤링 실행 후 잠시 대기

---

## 우리가 배운 점

### 1. 조건부 빈 생성의 중요성

OAuth2처럼 선택적 기능은 `@Conditional`을 사용하여 환경 변수 존재 시에만 활성화해야 한다. 이를 통해 불필요한 오류를 방지할 수 있다.

### 2. 세션과 JWT의 혼용

OAuth2는 세션 기반이므로, JWT 기반 인증과 혼용할 때는 경로별로 세션 정책을 다르게 설정해야 한다.

### 3. 데이터베이스 호환성

H2와 PostgreSQL의 차이점(ENUM 처리, 타입 변환 등)을 고려하여 개발해야 한다. `SchemaFix` 같은 유틸리티로 개발 환경의 불일치를 해결할 수 있다.

### 4. 사용자 경험 최적화

크롤러의 `fast` 모드처럼, 사용자가 기다리는 시간을 줄이는 것이 중요하다. 선택적 상세 정보 추출로 성능과 품질의 균형을 맞출 수 있다.

### 5. 역할 기반 접근 제어

JWT에 역할 정보를 포함하면 프론트엔드에서도 권한 기반 UI를 쉽게 구현할 수 있다.

### 6. 검색 기능의 확장성

JOIN을 활용하면 관련 엔티티의 데이터도 검색 대상에 포함시킬 수 있어, 사용자 경험이 크게 향상된다.

### 7. 스마트 캐스트 주의사항

Kotlin에서 mutable property는 스마트 캐스트가 불가능하다. 로컬 변수에 할당하여 해결할 수 있다.

---

## 추가 리소스

- [OAuth2 설정 가이드](./README_OAUTH2.md)
- [프론트엔드 README](../rev-frontend/README.md)
- [Swagger UI](http://localhost:8080/swagger-ui/index.html)

---

## 기여하기

이 프로젝트는 학습 목적으로 개발되었습니다. 버그 리포트나 개선 제안은 언제든 환영합니다!

---

마지막 업데이트: 2025-01-01  
버전: 1.0.0

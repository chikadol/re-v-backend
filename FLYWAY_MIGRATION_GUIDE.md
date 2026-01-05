# Flyway 마이그레이션 가이드

## 문제 해결

Flyway Gradle 플러그인에서 PostgreSQL 드라이버를 찾지 못하는 문제가 발생할 수 있습니다.

## 해결 방법

### 방법 1: Spring Boot 자동 마이그레이션 사용 (권장)

Spring Boot 애플리케이션을 실행하면 자동으로 Flyway 마이그레이션이 실행됩니다.

1. `application.yml`에서 `flyway.enabled: true` 설정 확인
2. 애플리케이션 실행:
   ```bash
   ./gradlew bootRun
   ```

애플리케이션이 시작되면 자동으로 마이그레이션이 실행됩니다.

### 방법 2: Gradle 플러그인 사용 (수동 설정 필요)

Gradle 플러그인을 사용하려면 다음 설정이 필요합니다:

1. `gradle.properties`에 데이터베이스 정보 설정 (이미 완료됨)
2. `build.gradle.kts`의 `flyway` 블록 확인

**주의**: Flyway Gradle 플러그인은 때때로 PostgreSQL 드라이버를 찾지 못할 수 있습니다. 이 경우 방법 1을 사용하는 것이 더 안정적입니다.

### 방법 3: Flyway CLI 사용

Flyway CLI를 직접 사용할 수도 있습니다:

```bash
# Flyway CLI 설치 (Homebrew)
brew install flyway

# 마이그레이션 실행
flyway -url=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres \
       -user=postgres.gsmbdibjiuwdqhrdvsfo \
       -password=chikadol123! \
       -schemas=rev \
       migrate
```

## 현재 설정 상태

- ✅ `application.yml`: `flyway.enabled: true` (자동 마이그레이션 활성화)
- ✅ `gradle.properties`: Flyway 설정 완료
- ✅ `build.gradle.kts`: Flyway 플러그인 설정 완료

## 마이그레이션 실행

### 자동 마이그레이션 (권장)
```bash
# 애플리케이션 실행 시 자동으로 마이그레이션 실행
./gradlew bootRun
```

### 수동 확인
마이그레이션 상태를 확인하려면:
```bash
# 애플리케이션 실행 후 로그에서 Flyway 마이그레이션 메시지 확인
# 또는 데이터베이스에서 flyway_schema_history 테이블 확인
```

## V27 인덱스 마이그레이션

새로 추가된 인덱스 마이그레이션 파일:
- `V27__add_performance_indexes.sql`

이 마이그레이션은 다음 인덱스를 추가합니다:
- Thread 테이블: board_id+is_private, author_id, created_at, title
- ThreadTag 테이블: tag_id, thread_id
- Comment 테이블: thread_id, author_id
- ThreadBookmark 테이블: user_id, thread_id
- ThreadReaction 테이블: thread_id, user_id, 복합 인덱스
- Notification 테이블: user_id, user_id+is_read, created_at

## 문제 해결

### "No database found to handle jdbc:postgresql://..." 오류

이 오류는 Flyway Gradle 플러그인이 PostgreSQL 드라이버를 찾지 못할 때 발생합니다.

**해결책**: Spring Boot 자동 마이그레이션 사용 (방법 1)

### 연결 실패

- 데이터베이스 서버가 실행 중인지 확인
- 네트워크 연결 확인
- 방화벽 설정 확인
- `gradle.properties`의 연결 정보 확인

---

**권장 방법**: Spring Boot 자동 마이그레이션 사용 (`./gradlew bootRun`)


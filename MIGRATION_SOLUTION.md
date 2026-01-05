# Flyway 마이그레이션 해결 방법

## 문제

Flyway Gradle 플러그인(`./gradlew flywayMigrate`)이 PostgreSQL 드라이버를 찾지 못하는 오류가 발생합니다.

## 해결 방법: Spring Boot 자동 마이그레이션 사용 (권장)

Spring Boot 애플리케이션을 실행하면 자동으로 Flyway 마이그레이션이 실행됩니다.

### 설정 확인

`application.yml`에서 다음 설정이 활성화되어 있습니다:

```yaml
flyway:
  enabled: true  # 애플리케이션 시작 시 자동 마이그레이션
  baseline-on-migrate: true
  locations: classpath:db/migration
  schemas: rev
```

### 마이그레이션 실행

애플리케이션을 실행하면 자동으로 마이그레이션이 실행됩니다:

```bash
./gradlew bootRun
```

애플리케이션이 시작되면:
1. Flyway가 데이터베이스 연결 확인
2. `db/migration` 폴더의 마이그레이션 파일 확인
3. 아직 실행되지 않은 마이그레이션 자동 실행
4. V27 인덱스 마이그레이션 포함하여 모든 마이그레이션 적용

### 마이그레이션 확인

애플리케이션 로그에서 다음 메시지를 확인할 수 있습니다:

```
Flyway Community Edition 10.17.0 by Redgate
Database: jdbc:postgresql://... (PostgreSQL 15)
Successfully validated 27 migrations (execution time 00:00.123s)
Current version of schema "rev": 26
Migrating schema "rev" to version "27 - add performance indexes"
Successfully applied 1 migration to schema "rev" (execution time 00:00.456s)
```

또는 데이터베이스에서 직접 확인:

```sql
SELECT * FROM rev.flyway_schema_history ORDER BY installed_rank;
```

## 대안: Flyway CLI 사용

Gradle 플러그인 대신 Flyway CLI를 사용할 수도 있습니다:

```bash
# Flyway CLI 설치 (Homebrew)
brew install flyway

# 마이그레이션 실행
flyway -url=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres \
       -user=postgres.gsmbdibjiuwdqhrdvsfo \
       -password=chikadol123! \
       -schemas=rev \
       -locations=filesystem:src/main/resources/db/migration \
       migrate
```

## V27 인덱스 마이그레이션

새로 추가된 인덱스 마이그레이션 파일:
- `V27__add_performance_indexes.sql`

이 마이그레이션은 성능 최적화를 위한 인덱스를 추가합니다:
- Thread, ThreadTag, Comment, ThreadBookmark, ThreadReaction, Notification 테이블

## 권장 사항

✅ **Spring Boot 자동 마이그레이션 사용** (`./gradlew bootRun`)
- 가장 안정적이고 간단한 방법
- 이미 설정되어 있음
- 애플리케이션 실행 시 자동 실행

---

**다음 단계**: `./gradlew bootRun`을 실행하여 애플리케이션을 시작하면 자동으로 마이그레이션이 실행됩니다.


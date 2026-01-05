# 빠른 시작 가이드

## Flyway 마이그레이션 실행

### ✅ 권장 방법: Spring Boot 자동 마이그레이션

애플리케이션을 실행하면 자동으로 마이그레이션이 실행됩니다:

```bash
./gradlew bootRun
```

**장점:**
- 가장 안정적이고 간단함
- PostgreSQL 드라이버 자동 인식
- 이미 설정되어 있음 (`application.yml`에서 `flyway.enabled: true`)

**실행 결과:**
- 애플리케이션 시작 시 Flyway가 자동으로 마이그레이션 실행
- V27 인덱스 마이그레이션 포함하여 모든 마이그레이션 적용
- 로그에서 마이그레이션 실행 상태 확인 가능

### 대안: Flyway CLI 사용

Gradle 플러그인 대신 Flyway CLI를 사용할 수 있습니다:

```bash
# 1. Flyway CLI 설치 (Homebrew)
brew install flyway

# 2. 마이그레이션 실행
flyway -url=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres \
       -user=postgres.gsmbdibjiuwdqhrdvsfo \
       -password=chikadol123! \
       -schemas=rev \
       -locations=filesystem:src/main/resources/db/migration \
       migrate
```

### ❌ 비권장: Gradle 플러그인

`./gradlew flywayMigrate`는 PostgreSQL 드라이버를 찾지 못하는 문제가 있습니다.

**오류:**
```
No database found to handle jdbc:postgresql://...
```

이는 Flyway Gradle 플러그인의 알려진 제한사항입니다.

## 마이그레이션 확인

### 방법 1: 애플리케이션 로그 확인

`./gradlew bootRun` 실행 시 로그에서 확인:

```
Flyway Community Edition 10.17.0 by Redgate
Database: jdbc:postgresql://... (PostgreSQL 15)
Successfully validated 27 migrations
Current version of schema "rev": 26
Migrating schema "rev" to version "27 - add performance indexes"
Successfully applied 1 migration to schema "rev"
```

### 방법 2: 데이터베이스 직접 확인

```sql
SELECT * FROM rev.flyway_schema_history 
ORDER BY installed_rank DESC 
LIMIT 5;
```

V27 마이그레이션이 적용되었는지 확인:
```sql
SELECT version, description, installed_on 
FROM rev.flyway_schema_history 
WHERE version = '27';
```

## 다음 단계

1. ✅ **마이그레이션 실행**: `./gradlew bootRun`
2. ✅ **마이그레이션 확인**: 로그 또는 데이터베이스에서 확인
3. ✅ **애플리케이션 사용**: 정상적으로 작동하는지 확인

---

**권장**: `./gradlew bootRun`을 사용하여 자동 마이그레이션 실행


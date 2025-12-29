# Supabase 데이터베이스 연결 설정

## 문제
Supabase의 연결 문자열 형식 `postgresql://user:password@host:port/database`를 
Spring Boot의 JDBC URL 형식 `jdbc:postgresql://host:port/database`로 변환해야 합니다.

## 해결 방법

### 방법 1: 환경 변수로 설정 (권장)

Supabase 연결 문자열에서 정보를 추출하여 환경 변수로 설정:

```bash
# Supabase 연결 문자열 예시:
# postgresql://postgres:chikadol123!@db.gsmbdibjiuwdqhrdvsfo.supabase.co:5432/postgres

# 환경 변수 설정
export DATABASE_URL="jdbc:postgresql://db.gsmbdibjiuwdqhrdvsfo.supabase.co:5432/postgres"
export DATABASE_USER="postgres"
export DATABASE_PASSWORD="chikadol123!"
```

**주의**: 비밀번호에 특수문자(`!`, `@`, `#` 등)가 있으면 URL 인코딩이 필요할 수 있습니다.
- `!` → `%21`
- `@` → `%40`
- `#` → `%23`

### 방법 2: application.yml 직접 수정

`src/main/resources/application.yml` 파일에서:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.gsmbdibjiuwdqhrdvsfo.supabase.co:5432/postgres
    username: postgres
    password: chikadol123!
```

### 방법 3: URL 인코딩 사용 (비밀번호에 특수문자가 있는 경우)

비밀번호에 특수문자가 있으면 URL에 포함시킬 수도 있습니다:

```bash
# 비밀번호: chikadol123!
# URL 인코딩: chikadol123%21
export DATABASE_URL="jdbc:postgresql://postgres:chikadol123%21@db.gsmbdibjiuwdqhrdvsfo.supabase.co:5432/postgres"
```

하지만 이 방법보다는 username과 password를 분리하는 것이 더 안전합니다.

## Supabase 연결 정보 확인

1. Supabase 대시보드에 로그인
2. Project Settings → Database
3. Connection string에서 정보 확인:
   - Host: `db.gsmbdibjiuwdqhrdvsfo.supabase.co`
   - Port: `5432`
   - Database: `postgres`
   - User: `postgres`
   - Password: Supabase에서 제공한 비밀번호

## 테스트

설정 후 연결 테스트:

```bash
psql "postgresql://postgres:chikadol123!@db.gsmbdibjiuwdqhrdvsfo.supabase.co:5432/postgres"
```

또는 JDBC 형식으로:

```bash
psql -h db.gsmbdibjiuwdqhrdvsfo.supabase.co -p 5432 -U postgres -d postgres
```

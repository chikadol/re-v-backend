# 데이터베이스 설정 가이드

## PostgreSQL 데이터베이스 설정

### 1. PostgreSQL 설치 확인

PostgreSQL이 설치되어 있는지 확인:
```bash
psql --version
```

### 2. 데이터베이스 및 사용자 생성

PostgreSQL에 접속하여 데이터베이스와 사용자를 생성:

```bash
# PostgreSQL에 접속 (postgres 사용자로)
psql -U postgres

# 또는 sudo로 접속
sudo -u postgres psql
```

PostgreSQL 프롬프트에서 다음 명령 실행:

```sql
-- 데이터베이스 생성
CREATE DATABASE rev_db;

-- 사용자 생성 및 비밀번호 설정
CREATE USER rev_user WITH PASSWORD 'rev_password';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE rev_db TO rev_user;

-- 스키마 생성 권한 부여
\c rev_db
GRANT ALL ON SCHEMA rev TO rev_user;
CREATE SCHEMA IF NOT EXISTS rev;

-- 종료
\q
```

### 3. 환경 변수 설정

#### 방법 1: 환경 변수로 설정

```bash

export DATABASE_URL="postgresql://postgres:chikadol123!@db.gsmbdibjiuwdqhrdvsfo.supabase.co:5432/postgres"
export DATABASE_USER="chikadol123!"
export DATABASE_PASSWORD="postgres"
```

#### 방법 2: application.yml 직접 수정

`src/main/resources/application.yml` 파일에서 직접 수정:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rev_db
    username: rev_user
    password: your_actual_password
```

### 4. 연결 테스트

```bash
psql -U rev_user -d rev_db -h localhost
```

비밀번호를 입력하여 연결이 되는지 확인합니다.

### 5. 기존 데이터베이스 사용

이미 데이터베이스가 있다면, 해당 데이터베이스의 사용자명과 비밀번호를 사용하세요.

### 문제 해결

#### "password authentication failed" 오류

1. PostgreSQL 사용자 비밀번호 확인:
   ```sql
   ALTER USER rev_user WITH PASSWORD 'new_password';
   ```

2. `pg_hba.conf` 파일 확인 (인증 방식 설정):
   - 위치: `/etc/postgresql/*/main/pg_hba.conf` (Linux)
   - 또는: `/usr/local/var/postgres/pg_hba.conf` (macOS Homebrew)
   - `local` 및 `host` 항목이 `md5` 또는 `password`로 설정되어 있는지 확인

3. PostgreSQL 재시작:
   ```bash
   # macOS (Homebrew)
   brew services restart postgresql
   
   # Linux
   sudo systemctl restart postgresql
   ```

#### "database does not exist" 오류

데이터베이스를 생성하지 않았다면 위의 2단계를 실행하세요.

#### "permission denied" 오류

사용자에게 충분한 권한이 없는 경우:
```sql
GRANT ALL PRIVILEGES ON DATABASE rev_db TO rev_user;
GRANT ALL ON SCHEMA rev TO rev_user;
ALTER USER rev_user CREATEDB;  -- 필요시
```

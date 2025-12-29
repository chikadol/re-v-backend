# PostgreSQL 데이터베이스 시작 및 설정 가이드

## 1. PostgreSQL 서버 시작

### macOS (Homebrew)
```bash
# PostgreSQL 서비스 시작
brew services start postgresql@14
# 또는
brew services start postgresql

# 서비스 상태 확인
brew services list | grep postgresql
```

### Linux
```bash
sudo systemctl start postgresql
# 또는
sudo service postgresql start
```

## 2. 데이터베이스 및 사용자 생성

PostgreSQL 서버가 실행된 후:

```bash
# PostgreSQL에 접속
psql -U postgres

# 또는 sudo로 접속 (macOS)
sudo -u postgres psql
```

PostgreSQL 프롬프트에서 다음 명령 실행:

```sql
-- 데이터베이스 생성
CREATE DATABASE rev_db;

-- 사용자 생성
CREATE USER rev_user WITH PASSWORD 'rev_password';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE rev_db TO rev_user;

-- 데이터베이스에 연결
\c rev_db

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS rev;

-- 스키마 권한 부여
GRANT ALL ON SCHEMA rev TO rev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rev GRANT ALL ON TABLES TO rev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rev GRANT ALL ON SEQUENCES TO rev_user;

-- 종료
\q
```

## 3. 빠른 설정 (스크립트 사용)

```bash
# PostgreSQL 서버 시작
brew services start postgresql

# 잠시 대기 (서버 시작 시간)
sleep 3

# 데이터베이스 설정 스크립트 실행
psql -U postgres -f quick-setup-db.sql
```

## 4. 연결 테스트

```bash
psql -U rev_user -d rev_db -h localhost
```

비밀번호 `rev_password`를 입력하여 연결이 되는지 확인합니다.

## 5. 문제 해결

### "connection to server on socket failed"
- PostgreSQL 서버가 실행되지 않았습니다.
- `brew services start postgresql` 실행

### "password authentication failed"
- 사용자 비밀번호가 맞지 않습니다.
- 사용자 비밀번호 재설정:
  ```sql
  ALTER USER rev_user WITH PASSWORD 'rev_password';
  ```

### "database does not exist"
- 데이터베이스를 생성하지 않았습니다.
- 위의 2단계를 실행하세요.

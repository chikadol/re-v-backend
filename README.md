# RE:V (리브) 백엔드 스타터 — Kotlin/Spring Boot + Supabase

1차 범위: 로그인 / 겐바 목록 / 아티스트 목록

## 빠른 시작
- Java 21, Gradle 8.x
- PostgreSQL (Supabase 권장)

```bash
# 환경 변수 또는 application.yml 수정
./gradlew bootRun
```

## 엔드포인트
- POST /auth/signup
- POST /auth/login
- POST /auth/refresh
- POST /auth/logout
- GET  /artists
- GET  /artists/{id}
- GET  /genbas
- GET  /genbas/{id}
```

## 주의
- application.yml의 DB/JWT 시크릿을 실제 값으로 변경하세요.
- Flyway가 `rev` 스키마를 자동 생성합니다.

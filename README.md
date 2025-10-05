# RE:V — Server Only (Swagger + Flyway + Env DB)

## 사용법
1) DB 실행
```bash
docker compose up -d
```
2) 환경변수 설정 (선택)
```bash
export DB_URL=jdbc:postgresql://localhost:5432/rev
export DB_USERNAME=rev
export DB_PASSWORD=rev
```
3) 서버 실행
```bash
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health:     http://localhost:8080/health

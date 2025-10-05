# re-v-backend (Windows-ready)

## 실행 순서 (PowerShell)
1) JDK 21 설치 및 JAVA_HOME 설정 (README 상단 가이드 참고)
2) DB 실행: `docker compose up -d`
3) 프로젝트 루트에서: `.\gradlew.bat bootRun`
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health:     http://localhost:8080/health

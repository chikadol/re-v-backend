# re-v-backend (Windows-ready)

## 실행 순서 (PowerShell)
1) JDK 21 설치 및 JAVA_HOME 설정 (README 상단 가이드 참고)
2) DB 실행: `docker compose up -d`
3) 프로젝트 루트에서: `.\gradlew.bat bootRun`
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health:     http://localhost:8080/health

## 프론트엔드 연동

프론트엔드 프로젝트는 `../rev-frontend`에 위치해 있습니다.

### 프론트엔드 실행
```bash
cd ../rev-frontend
npm install
npm run dev
```

프론트엔드는 `http://localhost:5173`에서 실행됩니다.

### CORS 설정
백엔드는 `http://localhost:*`에서 오는 요청을 허용하도록 CORS가 설정되어 있습니다.

# re-v-backend (Windows-ready)

## 실행 순서

### Windows (PowerShell)
1) JDK 21 설치 및 JAVA_HOME 설정
2) DB 실행: `docker compose up -d`
3) (선택) OAuth2 설정: `.\setup-oauth.ps1` 또는 `.env` 파일 편집
4) 프로젝트 루트에서: `.\gradlew.bat bootRun`
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health:     http://localhost:8080/health

### macOS / Linux
1) JDK 21 설치 및 JAVA_HOME 설정
2) DB 실행: `docker compose up -d`
3) (선택) OAuth2 설정: `./setup-oauth.sh` 또는 `.env` 파일 편집
4) 프로젝트 루트에서:
   ```bash
   # .env 파일을 로드하고 서버 시작 (권장)
   ./start-with-env.sh
   
   # 또는 수동으로 환경 변수 로드 후 실행
   source load-env.sh && ./gradlew bootRun
   
   # 또는 직접 실행 (환경 변수 없이)
   ./gradlew bootRun
   ```

## 환경 변수 설정

### 기본 설정
프로젝트 루트에 `.env` 파일을 생성하거나 환경 변수를 설정하세요.

`.env.example` 파일을 참고하여 필요한 환경 변수를 설정할 수 있습니다:
```bash
# .env.example을 .env로 복사
cp .env.example .env
# .env 파일을 편집하여 실제 값 입력
```

### 소셜 로그인 설정 (선택사항)
구글, 네이버, 카카오 소셜 로그인을 사용하려면 OAuth2 클라이언트를 설정해야 합니다.

**상세 가이드**: [README_OAUTH2.md](./README_OAUTH2.md) 참고

**빠른 설정 방법**:

**방법 1: 설정 스크립트 사용 (권장)**
```bash
# macOS/Linux
./setup-oauth.sh

# Windows (PowerShell)
.\setup-oauth.ps1
```

**방법 2: .env 파일 직접 편집**
1. `.env.example` 파일을 `.env`로 복사
2. 각 플랫폼에서 OAuth2 클라이언트 ID와 Secret 발급
3. `.env` 파일에 발급받은 값 입력
4. 백엔드 재시작

**방법 3: 환경 변수 직접 설정**
```bash
# macOS/Linux
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export NAVER_CLIENT_ID="your-naver-client-id"
export NAVER_CLIENT_SECRET="your-naver-client-secret"
export KAKAO_CLIENT_ID="your-kakao-client-id"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"

# Windows (PowerShell)
$env:GOOGLE_CLIENT_ID="your-google-client-id"
$env:GOOGLE_CLIENT_SECRET="your-google-client-secret"
$env:NAVER_CLIENT_ID="your-naver-client-id"
$env:NAVER_CLIENT_SECRET="your-naver-client-secret"
$env:KAKAO_CLIENT_ID="your-kakao-client-id"
$env:KAKAO_CLIENT_SECRET="your-kakao-client-secret"
```

**참고**: 환경 변수가 설정되지 않으면 소셜 로그인은 자동으로 비활성화됩니다.

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

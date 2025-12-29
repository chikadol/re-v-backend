# OAuth2 소셜 로그인 설정 가이드

이 가이드는 RE-V 백엔드에서 구글, 네이버, 카카오 소셜 로그인을 설정하는 방법을 설명합니다.

## 빠른 시작

1. `.env.example` 파일을 `.env`로 복사
2. 각 소셜 로그인 플랫폼에서 OAuth2 클라이언트 ID와 Secret 발급
3. `.env` 파일에 발급받은 값 입력
4. 백엔드 재시작

## 상세 설정 가이드

### 1. Google OAuth2 설정

1. **Google Cloud Console 접속**
   - https://console.cloud.google.com/ 접속
   - Google 계정으로 로그인

2. **프로젝트 생성**
   - 상단 프로젝트 선택 > 새 프로젝트
   - 프로젝트 이름 입력 후 생성

3. **OAuth 동의 화면 설정**
   - 좌측 메뉴: API 및 서비스 > OAuth 동의 화면
   - 사용자 유형: 외부 선택
   - 앱 정보 입력 (앱 이름, 사용자 지원 이메일 등)
   - 범위: `openid`, `profile`, `email` 자동 추가됨
   - 테스트 사용자 추가 (선택사항)

4. **OAuth 2.0 클라이언트 ID 생성**
   - 좌측 메뉴: API 및 서비스 > 사용자 인증 정보
   - 상단 "사용자 인증 정보 만들기" > "OAuth 2.0 클라이언트 ID"
   - 애플리케이션 유형: 웹 애플리케이션
   - 승인된 리디렉션 URI 추가:
     ```
     http://localhost:8080/login/oauth2/code/google
     ```
   - 생성 후 클라이언트 ID와 클라이언트 보안 비밀번호 복사

5. **환경 변수 설정**
   ```bash
   export GOOGLE_CLIENT_ID="발급받은-클라이언트-ID"
   export GOOGLE_CLIENT_SECRET="발급받은-클라이언트-보안-비밀번호"
   ```

### 2. Naver OAuth2 설정

1. **네이버 개발자 센터 접속**
   - https://developers.naver.com/apps/#/register 접속
   - 네이버 계정으로 로그인

2. **애플리케이션 등록**
   - "애플리케이션 등록" 클릭
   - 애플리케이션 이름 입력
   - 사용 API: "네이버 로그인" 선택
   - 서비스 URL: `http://localhost:8080`
   - Callback URL: `http://localhost:8080/login/oauth2/code/naver`
   - 등록 완료

3. **클라이언트 ID와 Secret 확인**
   - 등록한 애플리케이션 클릭
   - Client ID와 Client Secret 복사

4. **환경 변수 설정**
   ```bash
   export NAVER_CLIENT_ID="발급받은-클라이언트-ID"
   export NAVER_CLIENT_SECRET="발급받은-클라이언트-Secret"
   ```

### 3. Kakao OAuth2 설정

1. **카카오 개발자 센터 접속**
   - https://developers.kakao.com/ 접속
   - 카카오 계정으로 로그인

2. **애플리케이션 추가**
   - "내 애플리케이션" > "애플리케이션 추가하기"
   - 앱 이름, 사업자명 입력 후 저장

3. **플랫폼 설정**
   - 추가한 애플리케이션 선택
   - "플랫폼" 메뉴 클릭
   - "Web 플랫폼 등록" 클릭
   - 사이트 도메인: `http://localhost:8080`

4. **카카오 로그인 활성화**
   - "제품 설정" > "카카오 로그인" 활성화
   - "카카오 로그인" 메뉴 클릭
   - Redirect URI 등록: `http://localhost:8080/login/oauth2/code/kakao`
   - 동의항목 설정: 닉네임, 이메일 필수 동의 설정

5. **REST API 키 확인**
   - "앱 키" 메뉴에서 REST API 키 확인 (이것이 Client ID)
   - "제품 설정" > "카카오 로그인" > "보안"에서 Client Secret 생성

6. **환경 변수 설정**
   ```bash
   export KAKAO_CLIENT_ID="발급받은-REST-API-키"
   export KAKAO_CLIENT_SECRET="발급받은-Client-Secret"
   ```

## .env 파일 사용 방법

### macOS / Linux
```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 편집하여 실제 값 입력
nano .env  # 또는 원하는 에디터 사용

# .env 파일을 환경 변수로 로드
export $(cat .env | grep -v '^#' | xargs)

# 또는 스크립트 실행 시 자동 로드
source .env && ./gradlew bootRun
```

### Windows (PowerShell)
```powershell
# .env 파일 생성
Copy-Item .env.example .env

# .env 파일 편집하여 실제 값 입력
notepad .env  # 또는 원하는 에디터 사용

# .env 파일을 환경 변수로 로드
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
    }
}
```

**참고**: Spring Boot는 기본적으로 `.env` 파일을 자동으로 읽지 않습니다. 
- **방법 1**: 위의 명령어로 환경 변수로 로드한 후 서버 실행
- **방법 2**: IDE에서 환경 변수 설정 (IntelliJ: Run Configuration > Environment variables)
- **방법 3**: 시스템 환경 변수로 설정

## 환경 변수 직접 설정 방법

### Windows (PowerShell)
```powershell
# 현재 세션에만 적용
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
$env:NAVER_CLIENT_ID="your-client-id"
$env:NAVER_CLIENT_SECRET="your-client-secret"
$env:KAKAO_CLIENT_ID="your-client-id"
$env:KAKAO_CLIENT_SECRET="your-client-secret"

# 영구 설정 (시스템 환경 변수)
[System.Environment]::SetEnvironmentVariable("GOOGLE_CLIENT_ID", "your-client-id", "User")
[System.Environment]::SetEnvironmentVariable("GOOGLE_CLIENT_SECRET", "your-client-secret", "User")
```

### macOS / Linux
```bash
# 현재 세션에만 적용
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
export NAVER_CLIENT_ID="your-client-id"
export NAVER_CLIENT_SECRET="your-client-secret"
export KAKAO_CLIENT_ID="your-client-id"
export KAKAO_CLIENT_SECRET="your-client-secret"

# 영구 설정 (~/.zshrc 또는 ~/.bashrc에 추가)
echo 'export GOOGLE_CLIENT_ID="your-client-id"' >> ~/.zshrc
echo 'export GOOGLE_CLIENT_SECRET="your-client-secret"' >> ~/.zshrc
echo 'export NAVER_CLIENT_ID="your-client-id"' >> ~/.zshrc
echo 'export NAVER_CLIENT_SECRET="your-client-secret"' >> ~/.zshrc
echo 'export KAKAO_CLIENT_ID="your-client-id"' >> ~/.zshrc
echo 'export KAKAO_CLIENT_SECRET="your-client-secret"' >> ~/.zshrc
source ~/.zshrc
```

### .env 파일 사용 (권장)

프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 추가:

```bash
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

**주의**: `.env` 파일은 Git에 커밋하지 마세요! `.gitignore`에 추가되어 있습니다.

## 확인 방법

1. 환경 변수 확인:
   ```bash
   # macOS/Linux
   echo $GOOGLE_CLIENT_ID
   
   # Windows (PowerShell)
   echo $env:GOOGLE_CLIENT_ID
   ```

2. 백엔드 서버 재시작:
   ```bash
   # macOS/Linux (.env 파일 자동 로드)
   export $(cat .env | grep -v '^#' | xargs) && ./gradlew bootRun
   
   # Windows (PowerShell)
   .\gradlew.bat bootRun
   ```

3. 로그에서 다음 메시지 확인:
   - OAuth2가 활성화된 경우: 정상적으로 시작됨
   - OAuth2가 비활성화된 경우: "⚠️ OAuth2 설정 오류" 메시지 없음 (정상)

4. 프론트엔드에서 소셜 로그인 버튼 클릭하여 테스트

## 문제 해결

### "No static resource login/oauth2/code/google" 에러
- 환경 변수가 제대로 설정되지 않았거나
- 백엔드 서버가 재시작되지 않았을 수 있습니다
- 환경 변수 확인: `echo $GOOGLE_CLIENT_ID` (macOS/Linux) 또는 `echo $env:GOOGLE_CLIENT_ID` (Windows)

### 소셜 로그인 버튼이 작동하지 않음
- 각 플랫폼의 Redirect URI가 정확히 설정되었는지 확인
- 백엔드 서버가 실행 중인지 확인
- 브라우저 콘솔에서 에러 메시지 확인

### 특정 플랫폼만 사용하고 싶은 경우
- 사용하지 않는 플랫폼의 환경 변수는 설정하지 않으면 됩니다
- 예: 구글만 사용하려면 `GOOGLE_CLIENT_ID`와 `GOOGLE_CLIENT_SECRET`만 설정

## 프로덕션 배포 시 주의사항

1. **Redirect URI 변경**
   - 프로덕션 도메인에 맞게 Redirect URI 변경
   - 예: `https://yourdomain.com/login/oauth2/code/google`

2. **환경 변수 보안**
   - 프로덕션 환경에서는 환경 변수나 시크릿 관리 서비스 사용
   - 코드에 직접 하드코딩하지 마세요

3. **HTTPS 필수**
   - 프로덕션에서는 반드시 HTTPS 사용
   - OAuth2 제공자들이 HTTPS를 요구할 수 있습니다

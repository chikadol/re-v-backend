# .env 파일을 로드하고 백엔드를 시작하는 스크립트 (PowerShell)

if (Test-Path .env) {
    Write-Host "📝 .env 파일을 환경 변수로 로드합니다..." -ForegroundColor Cyan
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$' -and $_ -notmatch '^#') {
            [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
    Write-Host "✅ 환경 변수 로드 완료" -ForegroundColor Green
    Write-Host ""
    Write-Host "설정된 OAuth2 클라이언트:" -ForegroundColor Yellow
    $googleId = [System.Environment]::GetEnvironmentVariable("GOOGLE_CLIENT_ID", "Process")
    $naverId = [System.Environment]::GetEnvironmentVariable("NAVER_CLIENT_ID", "Process")
    $kakaoId = [System.Environment]::GetEnvironmentVariable("KAKAO_CLIENT_ID", "Process")
    Write-Host "  GOOGLE_CLIENT_ID: $($googleId.Substring(0, [Math]::Min(20, $googleId.Length)))..."
    Write-Host "  NAVER_CLIENT_ID: $($naverId.Substring(0, [Math]::Min(20, $naverId.Length)))..."
    Write-Host "  KAKAO_CLIENT_ID: $($kakaoId.Substring(0, [Math]::Min(20, $kakaoId.Length)))..."
    Write-Host ""
} else {
    Write-Host "⚠️ .env 파일이 없습니다. .env.example을 복사하여 생성하세요." -ForegroundColor Red
    exit 1
}

Write-Host "🚀 백엔드 서버를 시작합니다..." -ForegroundColor Green
.\gradlew.bat bootRun

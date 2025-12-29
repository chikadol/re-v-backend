# OAuth2 환경 변수 설정 스크립트 (PowerShell)
# 사용법: .\setup-oauth.ps1

Write-Host "=== RE-V OAuth2 소셜 로그인 설정 ===" -ForegroundColor Cyan
Write-Host ""

# .env 파일 확인
if (-not (Test-Path .env)) {
    Write-Host ".env 파일이 없습니다. .env.example을 복사합니다..." -ForegroundColor Yellow
    Copy-Item .env.example .env
    Write-Host ".env 파일이 생성되었습니다." -ForegroundColor Green
    Write-Host ""
}

Write-Host "각 소셜 로그인 플랫폼에서 OAuth2 클라이언트를 발급받은 후"
Write-Host ".env 파일에 값을 입력하세요."
Write-Host ""
Write-Host "상세 가이드: README_OAUTH2.md 참고"
Write-Host ""
Write-Host "설정할 플랫폼을 선택하세요:"
Write-Host "1) Google"
Write-Host "2) Naver"
Write-Host "3) Kakao"
Write-Host "4) 모두 설정"
Write-Host "5) 종료"
Write-Host ""

$choice = Read-Host "선택 (1-5)"

switch ($choice) {
    "1" {
        $google_id = Read-Host "Google Client ID"
        $google_secret = Read-Host "Google Client Secret"
        if (Test-Path .env) {
            (Get-Content .env) -replace 'GOOGLE_CLIENT_ID=.*', "GOOGLE_CLIENT_ID=$google_id" | Set-Content .env
            (Get-Content .env) -replace 'GOOGLE_CLIENT_SECRET=.*', "GOOGLE_CLIENT_SECRET=$google_secret" | Set-Content .env
            Write-Host "Google OAuth2 설정이 완료되었습니다." -ForegroundColor Green
        }
    }
    "2" {
        $naver_id = Read-Host "Naver Client ID"
        $naver_secret = Read-Host "Naver Client Secret"
        if (Test-Path .env) {
            (Get-Content .env) -replace 'NAVER_CLIENT_ID=.*', "NAVER_CLIENT_ID=$naver_id" | Set-Content .env
            (Get-Content .env) -replace 'NAVER_CLIENT_SECRET=.*', "NAVER_CLIENT_SECRET=$naver_secret" | Set-Content .env
            Write-Host "Naver OAuth2 설정이 완료되었습니다." -ForegroundColor Green
        }
    }
    "3" {
        $kakao_id = Read-Host "Kakao Client ID"
        $kakao_secret = Read-Host "Kakao Client Secret"
        if (Test-Path .env) {
            (Get-Content .env) -replace 'KAKAO_CLIENT_ID=.*', "KAKAO_CLIENT_ID=$kakao_id" | Set-Content .env
            (Get-Content .env) -replace 'KAKAO_CLIENT_SECRET=.*', "KAKAO_CLIENT_SECRET=$kakao_secret" | Set-Content .env
            Write-Host "Kakao OAuth2 설정이 완료되었습니다." -ForegroundColor Green
        }
    }
    "4" {
        $google_id = Read-Host "Google Client ID"
        $google_secret = Read-Host "Google Client Secret"
        $naver_id = Read-Host "Naver Client ID"
        $naver_secret = Read-Host "Naver Client Secret"
        $kakao_id = Read-Host "Kakao Client ID"
        $kakao_secret = Read-Host "Kakao Client Secret"
        if (Test-Path .env) {
            $content = Get-Content .env
            $content = $content -replace 'GOOGLE_CLIENT_ID=.*', "GOOGLE_CLIENT_ID=$google_id"
            $content = $content -replace 'GOOGLE_CLIENT_SECRET=.*', "GOOGLE_CLIENT_SECRET=$google_secret"
            $content = $content -replace 'NAVER_CLIENT_ID=.*', "NAVER_CLIENT_ID=$naver_id"
            $content = $content -replace 'NAVER_CLIENT_SECRET=.*', "NAVER_CLIENT_SECRET=$naver_secret"
            $content = $content -replace 'KAKAO_CLIENT_ID=.*', "KAKAO_CLIENT_ID=$kakao_id"
            $content = $content -replace 'KAKAO_CLIENT_SECRET=.*', "KAKAO_CLIENT_SECRET=$kakao_secret"
            $content | Set-Content .env
            Write-Host "모든 OAuth2 설정이 완료되었습니다." -ForegroundColor Green
        }
    }
    "5" {
        Write-Host "종료합니다."
        exit 0
    }
    default {
        Write-Host "잘못된 선택입니다." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "설정이 완료되었습니다. 백엔드 서버를 재시작하세요." -ForegroundColor Green

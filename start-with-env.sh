#!/bin/bash
# .env 파일을 로드하고 백엔드를 시작하는 스크립트

if [ -f .env ]; then
    echo "📝 .env 파일을 환경 변수로 로드합니다..."
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
    echo "✅ 환경 변수 로드 완료"
    echo ""
    echo "설정된 OAuth2 클라이언트:"
    echo "  GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID:0:20}..."
    echo "  NAVER_CLIENT_ID: ${NAVER_CLIENT_ID:0:20}..."
    echo "  KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID:0:20}..."
    echo ""
else
    echo "⚠️ .env 파일이 없습니다. .env.example을 복사하여 생성하세요."
    exit 1
fi

echo "🚀 백엔드 서버를 시작합니다..."
./gradlew bootRun

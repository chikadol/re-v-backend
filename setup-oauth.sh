#!/bin/bash

# OAuth2 환경 변수 설정 스크립트
# 사용법: ./setup-oauth.sh

echo "=== RE-V OAuth2 소셜 로그인 설정 ==="
echo ""

# .env 파일 확인
if [ ! -f .env ]; then
    echo ".env 파일이 없습니다. .env.example을 복사합니다..."
    cp .env.example .env
    echo ".env 파일이 생성되었습니다."
    echo ""
fi

echo "각 소셜 로그인 플랫폼에서 OAuth2 클라이언트를 발급받은 후"
echo ".env 파일에 값을 입력하세요."
echo ""
echo "상세 가이드: README_OAUTH2.md 참고"
echo ""
echo "설정할 플랫폼을 선택하세요:"
echo "1) Google"
echo "2) Naver"
echo "3) Kakao"
echo "4) 모두 설정"
echo "5) 종료"
echo ""
read -p "선택 (1-5): " choice

case $choice in
    1)
        read -p "Google Client ID: " google_id
        read -p "Google Client Secret: " google_secret
        if [ -f .env ]; then
            sed -i.bak "s/GOOGLE_CLIENT_ID=.*/GOOGLE_CLIENT_ID=$google_id/" .env
            sed -i.bak "s/GOOGLE_CLIENT_SECRET=.*/GOOGLE_CLIENT_SECRET=$google_secret/" .env
            echo "Google OAuth2 설정이 완료되었습니다."
        fi
        ;;
    2)
        read -p "Naver Client ID: " naver_id
        read -p "Naver Client Secret: " naver_secret
        if [ -f .env ]; then
            sed -i.bak "s/NAVER_CLIENT_ID=.*/NAVER_CLIENT_ID=$naver_id/" .env
            sed -i.bak "s/NAVER_CLIENT_SECRET=.*/NAVER_CLIENT_SECRET=$naver_secret/" .env
            echo "Naver OAuth2 설정이 완료되었습니다."
        fi
        ;;
    3)
        read -p "Kakao Client ID: " kakao_id
        read -p "Kakao Client Secret: " kakao_secret
        if [ -f .env ]; then
            sed -i.bak "s/KAKAO_CLIENT_ID=.*/KAKAO_CLIENT_ID=$kakao_id/" .env
            sed -i.bak "s/KAKAO_CLIENT_SECRET=.*/KAKAO_CLIENT_SECRET=$kakao_secret/" .env
            echo "Kakao OAuth2 설정이 완료되었습니다."
        fi
        ;;
    4)
        read -p "Google Client ID: " google_id
        read -p "Google Client Secret: " google_secret
        read -p "Naver Client ID: " naver_id
        read -p "Naver Client Secret: " naver_secret
        read -p "Kakao Client ID: " kakao_id
        read -p "Kakao Client Secret: " kakao_secret
        if [ -f .env ]; then
            sed -i.bak "s/GOOGLE_CLIENT_ID=.*/GOOGLE_CLIENT_ID=$google_id/" .env
            sed -i.bak "s/GOOGLE_CLIENT_SECRET=.*/GOOGLE_CLIENT_SECRET=$google_secret/" .env
            sed -i.bak "s/NAVER_CLIENT_ID=.*/NAVER_CLIENT_ID=$naver_id/" .env
            sed -i.bak "s/NAVER_CLIENT_SECRET=.*/NAVER_CLIENT_SECRET=$naver_secret/" .env
            sed -i.bak "s/KAKAO_CLIENT_ID=.*/KAKAO_CLIENT_ID=$kakao_id/" .env
            sed -i.bak "s/KAKAO_CLIENT_SECRET=.*/KAKAO_CLIENT_SECRET=$kakao_secret/" .env
            echo "모든 OAuth2 설정이 완료되었습니다."
        fi
        ;;
    5)
        echo "종료합니다."
        exit 0
        ;;
    *)
        echo "잘못된 선택입니다."
        exit 1
        ;;
esac

echo ""
echo "설정이 완료되었습니다. 백엔드 서버를 재시작하세요."

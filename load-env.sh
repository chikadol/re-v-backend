#!/bin/bash
# .env 파일을 환경 변수로 로드하는 헬퍼 스크립트
# 사용법: source load-env.sh 또는 . load-env.sh

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
    echo ".env 파일이 로드되었습니다."
else
    echo ".env 파일이 없습니다. .env.example을 복사하여 생성하세요."
fi

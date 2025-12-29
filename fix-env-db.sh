#!/bin/bash
# .env 파일의 DATABASE_URL 수정

if [ -f .env ]; then
    # INIT 파라미터 제거 (H2가 자동으로 스키마를 생성하도록)
    sed -i.bak 's|;INIT=CREATE SCHEMA IF NOT EXISTS rev||' .env
    echo "✅ .env 파일의 DATABASE_URL 수정 완료"
    echo ""
    echo "수정된 DATABASE_URL:"
    grep DATABASE_URL .env
else
    echo "⚠️ .env 파일이 없습니다."
fi

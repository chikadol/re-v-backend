# 배포 체크리스트

## ✅ 완료된 작업

### Backend 최적화
- ✅ 쿼리 최적화 및 N+1 문제 해결
- ✅ Redis 캐싱 전략 구현
- ✅ 구조화된 로깅 시스템 구축
- ✅ Rate Limiting 구현
- ✅ 데이터베이스 인덱스 최적화 (마이그레이션 파일 생성)

### Frontend 최적화
- ✅ 코드 스플리팅 구현
- ✅ React 성능 최적화 (memo, useCallback)
- ✅ 테스트 환경 구축
- ✅ 빌드 최적화

### 테스트
- ✅ Backend 테스트 작성 및 통과
- ✅ Frontend 테스트 작성

## 🚀 배포 전 확인 사항

### 1. 데이터베이스 마이그레이션
```bash
# 인덱스 추가 마이그레이션 실행
./gradlew flywayMigrate

# 또는 프로덕션 환경에서
flyway migrate -url=jdbc:postgresql://... -user=... -password=...
```

### 2. 환경 변수 설정
```bash
# Redis 설정
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password

# 데이터베이스 설정
export DATABASE_URL=jdbc:postgresql://...
export DATABASE_USERNAME=...
export DATABASE_PASSWORD=...

# JWT 설정
export JWT_SECRET=your-secret-key-min-256-bits
```

### 3. 테스트 실행
```bash
# Backend 테스트
./gradlew test

# Frontend 테스트
cd ../rev-frontend
npm run test
```

### 4. 빌드 확인
```bash
# Backend 빌드
./gradlew build

# Frontend 빌드
cd ../rev-frontend
npm run build
```

### 5. 로그 디렉토리 생성
```bash
mkdir -p logs
chmod 755 logs
```

## 📋 프로덕션 환경 체크리스트

### 보안
- [ ] JWT_SECRET이 강력한 랜덤 문자열인지 확인
- [ ] 데이터베이스 비밀번호가 안전하게 관리되는지 확인
- [ ] Redis 비밀번호 설정 (선택사항)
- [ ] HTTPS 설정
- [ ] CORS 설정이 프로덕션 도메인으로 제한되어 있는지 확인

### 성능
- [ ] Redis 서버가 실행 중인지 확인
- [ ] 데이터베이스 연결 풀 설정 확인
- [ ] 로그 파일 디스크 공간 확인
- [ ] Rate Limiting 설정 확인

### 모니터링
- [ ] 로그 파일 경로 확인 (`logs/application.log`)
- [ ] 에러 로그 모니터링 설정
- [ ] 애플리케이션 헬스 체크 엔드포인트 확인

### 데이터베이스
- [ ] 마이그레이션 실행 완료 확인
- [ ] 인덱스 생성 확인
- [ ] 백업 전략 수립

## 🔧 문제 해결

### Redis 연결 실패
- Redis 서버가 실행 중인지 확인
- `REDIS_HOST`, `REDIS_PORT` 환경 변수 확인
- 방화벽 설정 확인

### 캐시가 작동하지 않음
- Redis 연결 확인
- `@EnableCaching` 어노테이션 확인
- 캐시 매니저 빈 생성 확인

### Rate Limiting이 작동하지 않음
- 인터셉터 등록 확인 (`WebMvcConfig`)
- Bucket4j 의존성 확인
- 로그에서 Rate Limit 메시지 확인

### 로그 파일이 생성되지 않음
- `logs/` 디렉토리 권한 확인
- `logback-spring.xml` 파일 확인
- 애플리케이션 로그 레벨 확인

## 📊 성능 모니터링

### 확인할 지표
- API 응답 시간
- 캐시 히트율
- 데이터베이스 쿼리 성능
- Rate Limit 트리거 횟수
- 에러 로그 발생 빈도

### 모니터링 도구
- 애플리케이션 로그 (`logs/application.log`)
- 에러 로그 (`logs/error.log`)
- 데이터베이스 쿼리 로그 (Hibernate SQL 로그)
- Redis 모니터링

## 🎯 다음 단계

1. **프로덕션 환경 배포**
   - Docker 컨테이너 빌드 및 배포
   - CI/CD 파이프라인 설정

2. **모니터링 구축**
   - ELK 스택 연동 (로그 수집)
   - Prometheus + Grafana (메트릭 수집)
   - APM 도구 통합

3. **성능 튜닝**
   - 실제 트래픽 기반 성능 측정
   - 쿼리 최적화 추가
   - 캐시 전략 조정

4. **보안 강화**
   - 보안 스캔 실행
   - 취약점 점검
   - 보안 헤더 추가

---

**마지막 업데이트**: 2026-01-04
**상태**: 배포 준비 완료 ✅


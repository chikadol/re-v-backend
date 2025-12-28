# 결제 시스템 설정 가이드

이 프로젝트는 네이버페이, 토스페이먼츠, 카카오페이의 실제 API를 사용하여 결제 시스템을 구현했습니다.

## 설정 방법

### 1. 환경 변수 설정

각 결제 서비스의 API 키를 환경 변수로 설정하거나 `application.yml`에 직접 입력하세요.

#### 네이버페이
```bash
export NAVER_PAY_CLIENT_ID="your-client-id"
export NAVER_PAY_CLIENT_SECRET="your-client-secret"
```

#### 토스페이먼츠
```bash
export TOSS_SECRET_KEY="test_sk_..." # 테스트 환경
# 또는
export TOSS_SECRET_KEY="live_sk_..." # 운영 환경
```

#### 카카오페이
```bash
export KAKAO_ADMIN_KEY="your-admin-key"
export KAKAO_CID="TC0ONETIME" # 테스트 환경: TC0ONETIME, 운영: 실제 가맹점 코드
```

### 2. API 키 발급 방법

#### 네이버페이
1. [네이버페이 개발자 센터](https://developers.pay.naver.com/) 접속
2. 애플리케이션 등록
3. 클라이언트 ID와 시크릿 키 발급

#### 토스페이먼츠
1. [토스페이먼츠 개발자 센터](https://developers.tosspayments.com/) 접속
2. 가맹점 등록
3. 테스트용 또는 운영용 시크릿 키 발급

#### 카카오페이
1. [카카오 개발자 센터](https://developers.kakao.com/) 접속
2. 애플리케이션 등록
3. REST API 키 발급 (Admin Key)
4. 카카오페이 파트너센터에서 가맹점 코드(CID) 발급

### 3. 테스트 환경

각 서비스는 테스트 환경을 제공합니다:

- **네이버페이**: `https://dev.apis.naver.com` (개발 환경)
- **토스페이먼츠**: `https://api.tosspayments.com` (테스트/운영 동일)
- **카카오페이**: `https://kapi.kakao.com` (테스트/운영 동일)

### 4. 결제 흐름

1. **결제 요청**: `POST /api/payments`
   - 결제 방법 선택 (NAVER_PAY, TOSS, KAKAO_PAY)
   - 응답에 `paymentUrl` 포함

2. **결제 페이지로 리다이렉트**
   - 프론트엔드에서 `paymentUrl`로 사용자를 리다이렉트

3. **결제 완료 후 콜백**
   - 각 결제 서비스에서 `/api/payments/callback`으로 리다이렉트
   - 자동으로 결제 승인 처리

4. **결제 승인 (수동)**: `POST /api/payments/{paymentId}/approve`
   - 필요 시 수동으로 결제 승인 처리

5. **결제 취소**: `POST /api/payments/{paymentId}/cancel`
   - 완료된 결제 취소

### 5. 주의사항

- **보안**: API 키는 절대 공개 저장소에 커밋하지 마세요
- **환경 변수**: 운영 환경에서는 환경 변수로 관리하세요
- **HTTPS**: 운영 환경에서는 반드시 HTTPS를 사용하세요
- **테스트**: 실제 서비스에 적용하기 전에 테스트 환경에서 충분히 검증하세요

### 6. API 문서

- [네이버페이 API 문서](https://developers.pay.naver.com/docs/v2/api)
- [토스페이먼츠 API 문서](https://docs.tosspayments.com/)
- [카카오페이 API 문서](https://developers.kakao.com/docs/latest/ko/kakaopay/overview)


# RE-V 프로젝트 TODO 리스트

## 현재 구현 상태 체크리스트

### 🔹 Frontend 기능

- [x] **사용자 정보 조회 (me)**
  - ✅ `/api/me` 엔드포인트 사용 중
  - ✅ `MePage`에서 사용자 정보 표시
  - 📝 개선 필요: 전역 상태로 관리하여 여러 컴포넌트에서 재사용

- [x] **로그인 유지 & 리다이렉트**
  - ✅ `PrivateRoute` 컴포넌트로 라우팅 보호
  - ✅ `localStorage`에 토큰 저장
  - 📝 개선 필요: 토큰 만료 시 자동 갱신 로직

- [x] **글로벌 상태 (Context / Recoil / Redux)**
  - ✅ `AuthContext`로 인증 상태 전역 관리
  - ✅ 사용자 정보 전역 상태로 관리
  - ✅ 로그인/로그아웃 함수 제공

- [x] **에러 핸들링 UI**
  - ✅ `ErrorBoundary` 컴포넌트 구현
  - ✅ `ErrorMessage` 컴포넌트로 통일된 에러 표시
  - ✅ 인라인/전체 화면 두 가지 스타일 지원

- [x] **로딩 스피너**
  - ✅ `LoadingSpinner` 컴포넌트 구현
  - ✅ small/medium/large 크기 지원
  - ✅ 전체 화면 오버레이 옵션

- [x] **라우팅 보호 (Private Routes)**
  - ✅ `PrivateRoute` 컴포넌트 구현됨
  - ✅ `AdminRoute` 컴포넌트로 역할 기반 라우팅 보호
  - ✅ 관리자 권한 확인 및 자동 리다이렉트

### 🔹 Backend 기능

- [x] **회원가입 API**
  - ✅ `POST /auth/register` 구현됨
  - ✅ `SignUpRequest` DTO 사용
  - ✅ 비밀번호 암호화 적용

- [x] **사용자 프로필 API**
  - ✅ `GET /api/me` 구현됨
  - ✅ `MeController`에서 사용자 정보 반환

- [x] **CRUD API (게시글 등)**
  - ✅ Thread CRUD API 구현됨
  - ✅ Comment CRUD API 구현됨
  - ✅ Board CRUD API 구현됨

- [x] **Validation (DTO 유효성 검사)**
  - ✅ `@Valid` 어노테이션 사용
  - ✅ `@NotNull`, `@NotBlank` 등 검증 적용
  - ✅ 한국어 커스텀 검증 메시지 통일
  - ✅ `GlobalExceptionHandler`에서 자동 메시지 변환

- [x] **Response format 통일**
  - ✅ `ApiResponse<T>` 래퍼 클래스 생성
  - ✅ 모든 컨트롤러에서 통일된 응답 형식 사용
  - ✅ `ResponseHelper` 유틸리티로 간편한 응답 생성

- [x] **페이징 API**
  - ✅ `Pageable` 사용 중
  - ✅ `PageResponse<T>` 통일된 페이징 응답 형식
  - ✅ 모든 페이징 API에서 통일된 메타데이터 반환 (totalElements, totalPages, number, size 등)

---

## 우선순위별 구현 계획

### 🔴 High Priority (즉시 필요) ✅ 완료

1. ✅ **Response format 통일 (Backend)**
   - ✅ `ApiResponse<T>` 래퍼 클래스 생성
   - ✅ 성공/실패 응답 형식 통일
   - ✅ 에러 코드 및 메시지 표준화
   - ✅ `ResponseHelper` 유틸리티로 간편한 응답 생성

2. ✅ **글로벌 상태 관리 (Frontend)**
   - ✅ Context API 사용
   - ✅ `AuthContext`로 사용자 정보 전역 상태로 관리
   - ✅ 인증 상태 전역 관리

3. ✅ **에러 핸들링 UI (Frontend)**
   - ✅ `ErrorBoundary` 컴포넌트 구현
   - ✅ `ErrorMessage` 컴포넌트로 통일된 에러 표시
   - ✅ API 에러 처리 유틸리티

### 🟡 Medium Priority (단기) ✅ 완료

4. ✅ **로딩 스피너 (Frontend)**
   - ✅ 통일된 `LoadingSpinner` 컴포넌트
   - ✅ 크기별 스타일 지원
   - ✅ 전체 화면 오버레이 옵션

5. ✅ **토큰 자동 갱신 (Frontend)**
   - ✅ Access Token 만료 시 자동 갱신
   - ✅ Refresh Token 만료 시 자동 로그아웃
   - ✅ API 인터셉터에서 토큰 갱신 처리

6. ✅ **Validation 메시지 통일 (Backend)**
   - ✅ 한국어 커스텀 검증 메시지 정의
   - ✅ `GlobalExceptionHandler`에서 자동 메시지 변환
   - ✅ 에러 응답 형식 통일

### 🟢 Low Priority (중기) ✅ 완료

7. ✅ **역할 기반 라우팅 보호 (Frontend)**
   - ✅ `AdminRoute` 컴포넌트로 Admin 전용 라우트 보호
   - ✅ 권한별 접근 제어

8. ✅ **페이징 메타데이터 통일 (Backend)**
   - ✅ `PageResponse<T>` 커스텀 페이징 응답 DTO
   - ✅ 총 개수, 페이지 정보 포함
   - ✅ 모든 페이징 API에 적용

9. **성능 최적화**
   - React.memo 활용
   - 코드 스플리팅
   - 이미지 최적화

---

## 구현 가이드

### 1. Response Format 통일 (Backend)

```kotlin
// 공통 응답 포맷
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ApiError? = null
)

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)
```

### 2. 글로벌 상태 관리 (Frontend)

**옵션 A: Context API**
```typescript
// contexts/AuthContext.tsx
export const AuthContext = createContext<AuthContextType | null>(null);

// contexts/UserContext.tsx
export const UserContext = createContext<UserContextType | null>(null);
```

**옵션 B: Recoil**
```typescript
// atoms/authAtom.ts
export const authState = atom({
  key: 'authState',
  default: { isAuthenticated: false, user: null }
});
```

### 3. 에러 핸들링 UI (Frontend)

```typescript
// components/ErrorBoundary.tsx
class ErrorBoundary extends React.Component {
  // 에러 캐치 및 표시
}

// components/ErrorMessage.tsx
export function ErrorMessage({ error }: { error: string }) {
  // 통일된 에러 메시지 UI
}
```

### 4. 로딩 스피너 (Frontend)

```typescript
// components/LoadingSpinner.tsx
export function LoadingSpinner({ size = 'medium' }: { size?: 'small' | 'medium' | 'large' }) {
  // 통일된 로딩 스피너
}
```

---

## 참고사항

- 현재 프로젝트는 Spring Boot + Kotlin (Backend), React + TypeScript (Frontend) 사용
- 인증은 JWT 기반
- OAuth2 지원 (Google 등)
- 데이터베이스는 PostgreSQL 사용

---

## 체크리스트 업데이트 방법

각 기능 구현 완료 시:
1. 체크박스 `[ ]`를 `[x]`로 변경
2. 구현 내용 간단히 기록
3. 다음 우선순위 항목으로 진행


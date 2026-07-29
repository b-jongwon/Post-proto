# FactHub Frontend — Stage 02 적용 가이드

## 이번 단계

- Spring Session 기반 로그인
- 회원가입
- `GET /api/auth/me` 로그인 복구
- CSRF 토큰 자동 발급 및 변경 요청 자동 첨부
- 로그아웃
- 백엔드 검증 오류와 중복 계정 오류 표시
- 로그인 사용자 헤더
- 보호된 `/me` 내 정보 페이지
- 모바일 로그인 상태 반영

## 적용

이 ZIP은 Stage 01을 포함한 누적 전체본입니다.

1. 실행 중인 Vite 서버를 `Ctrl + C`로 종료합니다.
2. 기존 `frontend`를 백업합니다.
3. ZIP 안의 `frontend` 폴더를 FactHub 프로젝트 루트에 배치합니다.
4. 프론트 의존성을 설치합니다.

```powershell
cd frontend
npm install
```

## 실행

백엔드를 먼저 `localhost:8080`으로 실행한 뒤 프론트를 실행합니다.

```powershell
npm run dev
```

브라우저 주소:

```text
http://localhost:5173
```

Vite 프록시가 `/api` 요청을 `http://localhost:8080`으로 전달합니다.

## 확인 순서

1. `/signup`에서 새 계정을 생성합니다.
2. 회원가입 완료 후 `/login`으로 이동하는지 확인합니다.
3. 로그인 후 홈 헤더에 닉네임이 표시되는지 확인합니다.
4. 브라우저를 새로고침해도 로그인이 유지되는지 확인합니다.
5. `/me`에서 이메일, 닉네임, 권한, 가입일을 확인합니다.
6. 로그아웃 후 `/me` 접근 시 `/login`으로 이동하는지 확인합니다.

## 품질 검사

```powershell
npm run lint
npm run build
```

둘 다 오류 없이 완료되어야 합니다.

## 백엔드 API 계약

```text
POST /api/auth/signup
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
GET  /api/csrf
```

회원가입과 로그인은 CSRF 제외이며, 로그아웃은 CSRF 토큰이 필요합니다. 프론트 API 계층이 이를 자동 처리합니다.

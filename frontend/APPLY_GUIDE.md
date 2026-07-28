# FactHub Frontend Stage 01 적용 가이드

## 이번 단계

- Vite 기본 예제 제거
- React Router 적용
- TanStack Query 기본 설정
- `@/` 경로 별칭
- `/api` → `http://localhost:8080` 개발 프록시
- FactHub 디자인 토큰 및 반응형 레이아웃
- 홈, 로그인, 회원가입, 404 화면
- 재사용 UI 컴포넌트

로그인과 회원가입 폼은 이번 단계에서는 UI만 제공하며, 실제 API 연결은 Stage 02에서 진행합니다.

## 적용 방법

1. 실행 중인 Vite 서버를 `Ctrl + C`로 종료합니다.
2. 기존 프로젝트의 `frontend` 폴더를 백업하거나 삭제합니다.
3. 이 ZIP의 `frontend` 폴더를 Spring Boot 프로젝트 루트에 복사합니다.
4. 터미널에서 다음 명령을 실행합니다.

```powershell
cd frontend
npm install
npm run dev
```

5. 브라우저에서 `http://localhost:5173`에 접속합니다.

## 확인 화면

- `/` : FactHub 홈
- `/login` : 로그인 UI
- `/signup` : 회원가입 UI
- 존재하지 않는 주소 : 404 UI

## 품질 검사

```powershell
npm run lint
npm run build
```

두 명령이 모두 성공해야 정상입니다.

## 주의

- `node_modules`는 ZIP에 포함하지 않았습니다.
- 백엔드가 꺼져 있어도 Stage 01 화면은 볼 수 있습니다.
- 첫 설치 시 `npm install`이 `package-lock.json`을 생성합니다. 생성된 파일은 Git에 함께 커밋하세요.

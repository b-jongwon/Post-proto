# FactHub

FactHub는 커뮤니티 게시글의 핵심 주장을 Google Gemini와 공개 출처로 분석하고,
근거·출처·신뢰도와 함께 보여주는 팩트체크 서비스입니다.

## 주요 기능

- 이메일 인증 기반 회원가입과 세션 로그인
- 게시글 작성·수정·삭제·검색·정렬
- 댓글, 좋아요, 조회수, 알림
- Gemini 기반 팩트체크와 분석 이력
- 최신·인기·좋아요·조회수 게시글 탐색
- 작성 글·댓글·좋아요 글을 모아보는 마이페이지
- 게시글 링크·SNS·QR 공유
- 게시글 숨김, 회원 정지, AI 재분석을 제공하는 관리자 화면
- Docker Compose와 GitHub Actions 기반 EC2 배포

## 기술 구성

- Frontend: React, TypeScript, Vite, TanStack Query, Axios
- Backend: Java 21, Spring Boot, Spring Security, JPA
- Database: MySQL, Flyway
- AI: Google Gemini
- Runtime: Docker Compose, Nginx

## 로컬 실행

1. `.env.example`을 `.env`로 복사합니다.
2. DB 비밀번호와 Gemini API 키를 설정합니다.
3. Docker로 전체 서비스를 실행합니다.

```powershell
docker compose config
docker compose up -d --build
docker compose ps
```

접속 주소:

- 웹: `http://localhost`
- 헬스 체크: `http://localhost/actuator/health`

## 이메일 인증

로컬 개발에서는 `.env`의 `MAIL_DELIVERY_MODE=log`를 사용할 수 있습니다.
이 모드에서는 회원가입 화면이 개발용 인증 코드를 안내합니다.

운영에서는 반드시 `MAIL_DELIVERY_MODE=smtp`와 다음 값을 설정합니다.

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`

HTTPS를 적용한 운영 환경에서는 `SESSION_COOKIE_SECURE=true`를 사용합니다.

## 최초 관리자 지정

1. 일반 회원가입과 이메일 인증을 완료합니다.
2. `.env`의 `ADMIN_BOOTSTRAP_EMAIL`에 해당 이메일을 설정합니다.
3. 백엔드를 다시 시작해 관리자 승격을 확인합니다.
4. 승격 후 `.env`에서 `ADMIN_BOOTSTRAP_EMAIL` 값을 비웁니다.

## 검증 명령

```powershell
.\gradlew.bat test --no-daemon
cd frontend
npm run lint
npm run build
```

## 배포 흐름

`main` 브랜치에 푸시하면 GitHub Actions가 백엔드 테스트와 프런트 검증을 먼저
실행합니다. 모든 검증이 성공한 경우에만 EC2에서 최신 커밋을 받아 Docker Compose로
재빌드합니다. 이 저장소는 `.env`를 추적하지 않으므로 운영 서버의 `.env`는 별도로
관리해야 합니다.

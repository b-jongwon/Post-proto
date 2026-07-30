# FactHub 배포 체크리스트

## 최초 준비

1. `.env.example`을 `.env`로 복사합니다.
2. DB 비밀번호와 Gemini API 키를 입력합니다.
3. 운영 SMTP 계정과 발신 주소를 입력합니다.
4. HTTPS 적용 후 `SESSION_COOKIE_SECURE=true`로 변경합니다.
5. `.env`가 Git에서 무시되는지 `git check-ignore -v .env`로 확인합니다.

## 배포 전 검증

```powershell
.\gradlew.bat test --no-daemon
cd frontend
npm ci
npm run lint
npm run build
cd ..
docker compose config
```

`docker compose config` 출력에는 실제 비밀값이 포함될 수 있으므로 로그나 이슈에
붙여 넣지 않습니다.

## 실행

```powershell
docker compose up -d --build
docker compose ps
docker compose logs --tail 200 backend
```

## 확인

- 웹: `http://localhost`
- 백엔드 헬스: `http://localhost/actuator/health`
- 회원가입 이메일 발송
- 로그인과 로그아웃
- 게시글·댓글·좋아요
- Gemini 분석
- 관리자 화면

## 종료

```powershell
docker compose down
```

`docker compose down -v`는 MySQL 볼륨을 삭제하므로 운영 환경에서 사용하지 않습니다.

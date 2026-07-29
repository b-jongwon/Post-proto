# FactHub Docker 배포 1단계

## 파일 배치

facthub/
├─ Dockerfile
├─ .dockerignore
├─ docker-compose.yml
├─ .env
├─ .env.example
├─ nginx/
│  └─ default.conf
└─ frontend/
   ├─ Dockerfile
   └─ .dockerignore

## 최초 실행

1. `.env.example`을 `.env`로 복사
2. 실제 DB 비밀번호, MySQL root 비밀번호, Gemini API 키 입력
3. 아래 명령 실행

```powershell
docker compose config
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

## 접속

- 웹: http://localhost
- 백엔드 헬스: http://localhost/actuator/health

## 종료

```powershell
docker compose down
```

DB 데이터까지 완전히 삭제:

```powershell
docker compose down -v
```

주의: `-v`를 붙이면 MySQL 데이터가 삭제된다.

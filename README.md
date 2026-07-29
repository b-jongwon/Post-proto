@"
# FactHub

FactHub는 사용자가 게시한 정보에 대해 AI 기반 팩트체크 분석을 제공하는 웹 서비스입니다.

## 기술 스택

- Frontend: React, TypeScript
- Backend: Spring Boot
- Database: MySQL
- AI: Google Gemini
- Deployment: AWS EC2, Docker Compose, Nginx
- CI/CD: GitHub Actions

## 주요 기능

- 회원가입 및 로그인
- 게시글 작성, 조회, 수정, 삭제
- AI 기반 팩트체크 분석
- 분석 결과 및 출처 제공
- Docker 기반 배포
- GitHub Actions 자동 배포

## 서비스 주소

http://3.105.221.231

## 실행 방법

```bash
docker compose up -d --build

배포 구조
GitHub main 브랜치 push
        ↓
GitHub Actions 실행
        ↓
EC2 서버 SSH 접속
        ↓
최신 코드 반영
        ↓
Docker 이미지 재빌드 및 컨테이너 실행


프로젝트 상태

현재 AWS EC2 환경에 배포되어 있으며, GitHub Actions를 통한 자동 배포를 구성하고 있습니다.
"@ | Set-Content -Encoding UTF8 README.md
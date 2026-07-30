# FactHub repository guide

## Architecture

- Backend: Java 21, Spring Boot, Spring Security, JPA, Flyway, MySQL
- Frontend: React, TypeScript, Vite, TanStack Query, Axios
- Runtime: Docker Compose with MySQL, backend, Nginx frontend
- Production entrypoint: Nginx proxies `/api` and `/actuator` to the backend

## Source layout

- Backend: `src/main/java/com/facthub`
- Backend configuration: `src/main/resources`
- Database migrations: `src/main/resources/db/migration`
- Backend tests: `src/test`
- Frontend: `frontend/src`
- Deployment: `docker-compose.yml`, `Dockerfile`, `frontend/Dockerfile`, `nginx`

## Required checks

- Backend: `.\gradlew.bat test --no-daemon`
- Frontend lint: run `npm run lint` in `frontend`
- Frontend build: run `npm run build` in `frontend`
- For schema changes, add a new Flyway migration. Never edit an applied migration.

## Engineering rules

- Keep the existing `ApiResponse` response envelope.
- Preserve session authentication and CSRF protection unless a task explicitly changes the authentication architecture.
- Public reads and authenticated mutations must remain explicit in `SecurityConfig`.
- Avoid N+1 queries for list endpoints; batch aggregate counts and related records.
- Keep TypeScript API types synchronized with backend DTOs.
- Add or update tests for changed business rules.
- Do not push, deploy, rotate credentials, or alter production data unless explicitly requested.

## Secrets

- Never print, commit, or copy values from `.env`.
- Refer to secrets only through environment-variable names.
- `.env.example` must contain placeholders only.
- New client-side environment variables must never contain server API keys or credentials.

## Definition of done

- Requested behavior is implemented end to end.
- Relevant backend tests pass.
- Frontend lint and production build pass.
- Migrations are forward-only and safe for existing data.
- Documentation and environment examples reflect new required configuration.


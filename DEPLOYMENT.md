# Deployment Guide

Spring Boot backend for the marketplace system, packaged as a multi-stage Docker image.

## Requirements

- Java 21 (build only) or Docker
- PostgreSQL 15+ (or the bundled `db` container)

## Environment variables

The application runs with the `prod` Spring profile. All values are read from the
environment; required ones fail fast at startup when missing.

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `DB_URL` | yes | — | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/marketplace_system` |
| `DB_USERNAME` | yes | — | DB user |
| `DB_PASSWORD` | yes | — | DB password |
| `JWT_SECRET` | yes | — | HMAC key signing JWTs. Use a random string ≥ 64 chars (`openssl rand -hex 32`) |
| `ADMIN_INITIAL_PASSWORD` | yes | — | Initial password for the admin account (seeded on first boot) |
| `ADMIN_INITIAL_EMAIL` | no | `admin@marketplace.com` | Admin login email |
| `JWT_EXPIRATION_MS` | no | `86400000` | JWT lifetime in ms |
| `CORS_ALLOWED_ORIGINS` | no | empty (block all) | Comma-separated frontend origins, e.g. `https://shop.example.com,https://admin.example.com` |
| `SERVER_PORT` | no | `8080` | HTTP port |
| `SPRINGDOC_ENABLED` | no | `false` | Set `true` to expose Swagger UI / OpenAPI in prod |
| `LOG_FILE` | no | `logs/marketplace-system.log` | Log file path |

## Run with Docker (recommended)

```bash
cp .env.example .env   # then edit .env with real secrets
docker compose up -d --build
```

- App: `http://localhost:8080` (or `APP_PORT` from `.env`)
- Postgres: `localhost:5433` (`POSTGRES_PORT`)
- Health check: `GET http://localhost:8080/actuator/health`
- Readiness probe: `GET http://localhost:8080/actuator/health/readiness`
- Logs: `docker compose logs -f app`

## Run without Docker

```bash
export DB_URL='jdbc:postgresql://localhost:5432/marketplace_system'
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET='<random string >= 64 chars>'
export ADMIN_INITIAL_PASSWORD='<strong password>'
export CORS_ALLOWED_ORIGINS='http://127.0.0.1:5500'

mvn clean package -DskipTests
java -jar target/marketplace-system.jar --spring.profiles.active=prod
```

## Security notes

- The default `application.yml` contains **development-only** fallbacks (e.g. a
  well-known `JWT_SECRET`). The `prod` profile deliberately has **no fallbacks**
  for secrets so a misconfigured deploy refuses to start.
- Actuator exposes only `health` and `info`; health details are never shown.
- Swagger/OpenAPI is disabled unless `SPRINGDOC_ENABLED=true`.
- The container runs as a non-root user (`appuser`) and performs a graceful
  shutdown on `SIGTERM`.
- `ddl-auto: update` is kept for schema management (project decision). This is
  only safe for single-instance deployments; revisit before going multi-node.

## Useful commands

```bash
docker compose logs -f db        # DB logs
docker compose exec db psql -U postgres -d marketplace_system
docker compose down              # stop; add -v to also drop the data volume
```

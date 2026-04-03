# Finance Backend (Spring Boot + JWT + Swagger)

Backend APIs for a finance dashboard:
- Users, roles, access control
- Financial records (CRUD + filtering)
- Dashboard analytics (summary, category totals, trends)
- JWT authentication (access + refresh)
- Swagger/OpenAPI documentation

## Requirements covered
- **Roles**: `VIEWER` (read-only), `ANALYST` (read + analytics), `ADMIN` (full access)
- **Financial record fields**: amount, type (income/expense), category, date, notes
- **Dashboard**: total income, total expense, net balance, category totals, trends
- **Validation & reliability**: request validation, consistent HTTP status codes, soft delete

## Run with PostgreSQL (production-like)
1. Start PostgreSQL and create database `finance_db`.
2. Update credentials in `src/main/resources/application.properties`:
   - `spring.datasource.username`
   - `spring.datasource.password`
3. Run:

```powershell
mvn clean spring-boot:run
```

Flyway migrations live in `src/main/resources/db/migration/`.

## Authentication flow (JWT)
1. Register (public):
- `POST /api/v1/auth/register`
2. Login (public):
- `POST /api/v1/auth/login` → returns `token` (access) + `refreshToken`
3. Swagger: click **Authorize** and paste:
- `Bearer <token>`
4. Refresh token (public):
- `POST /api/v1/auth/refresh`

## API catalog (high level)

### Auth (`/api/v1/auth`) (public)
- `POST /register`
- `POST /login`
- `POST /refresh`

### Financial Records (`/api/v1/records`) (JWT required)
- `POST /` (Admin)
- `GET /` (Viewer/Analyst/Admin; non-admins only see their own)
- `GET /{id}` (Viewer/Analyst/Admin; non-admins only see their own)
- `PUT /{id}` (Admin)
- `DELETE /{id}` (Admin; soft delete)

### Dashboard (`/api/v1/dashboard`) (JWT required)
- `GET /summary` (Viewer/Analyst/Admin)
- `GET /categories` (Analyst/Admin)
- `GET /trends/daily` (Analyst/Admin)

### Admin Users (`/api/v1/admin/users`) (JWT required; Admin only)
- `GET /` (paged)
- `GET /{id}`
- `POST /`
- `PUT /{id}`
- `PATCH /{id}/role`
- `PATCH /{id}/status`
- `DELETE /{id}` (soft delete)

## Assumptions / tradeoffs
- **Non-admin data visibility**: non-admins are restricted to their own financial records.
- **Soft delete**: users and records are soft-deleted via `status=DELETED`.
- **Refresh tokens**: refresh tokens are JWTs and are not persisted/rotated (kept simple for assessment scope).


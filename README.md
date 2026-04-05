# Finance Backend

A production-ready backend for a **Finance Dashboard** system, built with **Java 21 + Spring Boot 3 + Spring Security + JWT + JPA/H2**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt 0.12) |
| ORM | Spring Data JPA + Hibernate |
| Database | H2 (file mode — data persisted in `./data/`) |
| Validation | Jakarta Bean Validation |
| Build | Maven 3.8+ |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

---

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+

### Run

```bash
git clone <repo-url>
cd finance-backend
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

On first startup the database is automatically seeded with 3 demo users and 22 sample financial records.

### Seeded Demo Accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@finance.dev | password123 |
| Analyst | analyst@finance.dev | password123 |
| Viewer | viewer@finance.dev | password123 |

---

## Useful URLs

| URL | Description |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API documentation |
| http://localhost:8080/h2-console | H2 database browser (JDBC URL: `jdbc:h2:file:./data/financedb`) |

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <token>
```

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register (default role: VIEWER) |
| POST | `/api/auth/login` | Public | Returns JWT token |
| GET | `/api/auth/me` | Any authenticated | Current user profile |

**Login example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@finance.dev","password":"password123"}'
```

---

### User Management (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update name / role / status |
| DELETE | `/api/users/{id}` | Deactivate user |

**Update user role:**
```bash
curl -X PUT http://localhost:8080/api/users/3 \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"role":"ANALYST","status":"ACTIVE"}'
```

---

### Financial Records

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/records` | Admin, Analyst | Create record |
| GET | `/api/records` | All roles | List (paginated + filterable) |
| GET | `/api/records/{id}` | All roles | Get single record |
| PUT | `/api/records/{id}` | Admin, Analyst | Update record |
| DELETE | `/api/records/{id}` | Admin | Soft delete |

**Filter parameters for `GET /api/records`:**

| Param | Type | Example |
|---|---|---|
| `type` | `INCOME` \| `EXPENSE` | `?type=EXPENSE` |
| `category` | string (partial match) | `?category=Salary` |
| `startDate` | `YYYY-MM-DD` | `?startDate=2026-01-01` |
| `endDate` | `YYYY-MM-DD` | `?endDate=2026-04-03` |
| `page` | int (0-based) | `?page=0` |
| `size` | int | `?size=10` |
| `sortBy` | field name | `?sortBy=amount` |
| `direction` | `ASC` \| `DESC` | `?direction=ASC` |

**Create a record:**
```bash
curl -X POST http://localhost:8080/api/records \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "type": "INCOME",
    "category": "Salary",
    "date": "2026-04-03",
    "notes": "April salary"
  }'
```

---

### Dashboard Analytics (Analyst + Admin)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | Analyst, Admin | Total income, expenses, net balance |
| GET | `/api/dashboard/by-category` | Analyst, Admin | Per-category totals |
| GET | `/api/dashboard/trends` | Analyst, Admin | Monthly totals (last 12 months) |
| GET | `/api/dashboard/recent` | All roles | Last 10 records |

---

## Role Permission Matrix

| Action | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Register / Login | ✅ | ✅ | ✅ |
| View own profile | ✅ | ✅ | ✅ |
| View records (list/detail) | ✅ | ✅ | ✅ |
| View recent activity | ✅ | ✅ | ✅ |
| Create / Update records | ❌ | ✅ | ✅ |
| Soft-delete records | ❌ | ❌ | ✅ |
| Dashboard summary & trends | ❌ | ✅ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

---

## Data Model

### User
```
id, name, email, password_hash, role (VIEWER|ANALYST|ADMIN),
status (ACTIVE|INACTIVE), created_at, updated_at
```

### financial_record
```
id, amount (DECIMAL 15,2), type (INCOME|EXPENSE), category, date,
notes, user_id (FK), created_at, updated_at, deleted_at (soft delete)
```

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-04-03T14:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to perform this action",
  "path": "/api/records"
}
```

Validation errors include a field-level breakdown:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "amount": "Amount is required",
    "date": "Date is required"
  },
  "path": "/api/records"
}
```

---

## Assumptions & Design Decisions

- **H2 file mode** is used for zero-config persistence. The DB file lives in `./data/financedb`. Switching to PostgreSQL/MySQL only requires changing the datasource URL and driver in `application.properties`.
- **Soft delete**: `deleted_at` timestamp is set on the record instead of physical deletion. The `@Where(clause = "deleted_at IS NULL")` Hibernate annotation ensures deleted records are automatically excluded from all standard queries.
- **New users default to VIEWER** role. An Admin must explicitly promote them.
- **JWT expiry** is 24 hours (`86400000 ms`). Refresh tokens are not implemented to keep scope focused.
- **Inactive users** cannot log in — the login endpoint checks the user's status before issuing a token.
- **Category is a free-text field** — no fixed enum — allowing flexibility without schema migrations.
- **Dashboard endpoints** operate on all non-deleted records system-wide (not scoped to the requesting user).

---

## Project Structure

```
src/main/java/com/finance/
├── FinanceApplication.java
├── config/
│   ├── DataInitializer.java   # Seed data on first run
│   ├── OpenApiConfig.java     # Swagger / JWT bearer scheme
│   └── SecurityConfig.java   # Spring Security filter chain
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── FinancialRecordController.java
│   └── UserController.java
├── dto/
│   ├── auth/   (LoginRequest, RegisterRequest, AuthResponse)
│   ├── record/ (RecordRequest, RecordResponse, RecordFilterParams)
│   └── user/   (UserResponse, UpdateUserRequest)
├── entity/
│   ├── FinancialRecord.java
│   └── User.java
├── enums/
│   ├── RecordType.java  (INCOME, EXPENSE)
│   ├── Role.java        (VIEWER, ANALYST, ADMIN)
│   └── UserStatus.java  (ACTIVE, INACTIVE)
├── exception/
│   ├── AppException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── FinancialRecordRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   ├── JwtUtil.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── DashboardService.java
    ├── FinancialRecordService.java
    └── UserService.java
```

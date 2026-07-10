# ExpenseFlow API

A personal finance tracking REST API built with Java 21 and Spring Boot 3. Designed to demonstrate production-grade backend patterns including JWT authentication, budget enforcement, monthly aggregation, and containerised deployment.

---

## Features

- User registration and login with JWT authentication
- Stateless session management with Spring Security
- Expense tracking organised by categories
- Monthly budget limits per category
- Real-time monthly spend aggregation per category
- Budget breach detection on every expense entry
- Paginated expense listing with sorting
- Date range filtering on expenses
- Full API documentation via Swagger UI
- Containerised with Docker and docker-compose
- CI pipeline with GitHub Actions

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Container | Docker + docker-compose |
| CI | GitHub Actions |

---

## Architecture

```
┌─────────────────────────────────────────┐
│           Client (Postman / Browser)    │
└──────────────────┬──────────────────────┘
                   │ HTTP + JWT Bearer Token
┌──────────────────▼──────────────────────┐
│         Spring Security Filter Chain    │
│      JwtAuthFilter — validates token,   │
│      sets authentication in context     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│              Controllers                │
│  AuthController  /api/v1/auth           │
│  CategoryController  /api/v1/categories │
│  ExpenseController  /api/v1/expenses    │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│               Services                  │
│  AuthService — registration and login   │
│  CategoryService — budget aggregation   │
│  ExpenseService — breach detection      │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│             Repositories                │
│  UserRepository                         │
│  CategoryRepository                     │
│  ExpenseRepository — custom JPQL query  │
│  for monthly spend aggregation          │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│            PostgreSQL 16                │
│  users / categories / expenses tables   │
└─────────────────────────────────────────┘
```

---

## Project Structure

```
expenseflow/
├── src/
│   └── main/
│       ├── java/com/gyanbiswal/expenseflow/
│       │   ├── config/          → SecurityConfig, SwaggerConfig
│       │   ├── controller/      → AuthController, CategoryController, ExpenseController
│       │   ├── service/         → AuthService, CategoryService, ExpenseService
│       │   ├── repository/      → UserRepository, CategoryRepository, ExpenseRepository
│       │   ├── model/           → User, Category, Expense, Role
│       │   ├── dto/
│       │   │   ├── request/     → RegisterRequest, LoginRequest, CategoryRequest, ExpenseRequest
│       │   │   └── response/    → AuthResponse, CategoryResponse, ExpenseResponse
│       │   ├── exception/       → GlobalExceptionHandler, custom exceptions
│       │   ├── security/        → JwtUtil, JwtAuthFilter, CustomUserDetailsService
│       │   └── util/            → SecurityUtils
│       └── resources/
│           └── application.properties
├── Dockerfile
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── ci.yml
└── README.md
```

---

## Running Locally

### Prerequisites

- Docker Desktop

### Start the application

```bash
docker compose up --build
```

PostgreSQL and the Spring Boot app both start automatically. No local Java or PostgreSQL installation required.

The API will be available at:

```
http://localhost:8080
```

### Stop the application

```bash
docker compose down
```

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

Click **Authorize**, paste your JWT token, and test all endpoints directly from the browser.

---

## API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/v1/auth/register | None | Register a new user |
| POST | /api/v1/auth/login | None | Login and receive JWT token |

#### Register

```json
POST /api/v1/auth/register

{
  "name": "Gyaan",
  "email": "gyaan@example.com",
  "password": "password123"
}
```

Response `201 Created`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "gyaan@example.com",
  "name": "Gyaan",
  "role": "USER"
}
```

#### Login

```json
POST /api/v1/auth/login

{
  "email": "gyaan@example.com",
  "password": "password123"
}
```

Response `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "gyaan@example.com",
  "name": "Gyaan",
  "role": "USER"
}
```

---

### Categories

All endpoints require `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/categories | Create a category with budget limit |
| GET | /api/v1/categories | Get all categories with monthly spend |
| GET | /api/v1/categories/{id} | Get category by ID |
| PUT | /api/v1/categories/{id} | Update category |
| DELETE | /api/v1/categories/{id} | Delete category |

#### Create Category

```json
POST /api/v1/categories

{
  "name": "Food",
  "budgetLimit": 5000.00
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "name": "Food",
  "budgetLimit": 5000.00,
  "spentThisMonth": 0.00,
  "budgetExceeded": false
}
```

#### Get All Categories

Response includes live monthly spend aggregation and breach status:

```json
[
  {
    "id": 1,
    "name": "Food",
    "budgetLimit": 5000.00,
    "spentThisMonth": 3450.00,
    "budgetExceeded": false
  },
  {
    "id": 2,
    "name": "Travel",
    "budgetLimit": 3000.00,
    "spentThisMonth": 3200.00,
    "budgetExceeded": true
  }
]
```

---

### Expenses

All endpoints require `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/expenses | Create an expense |
| GET | /api/v1/expenses | Get all expenses |
| GET | /api/v1/expenses/paged | Get paginated expenses |
| GET | /api/v1/expenses/{id} | Get expense by ID |
| PUT | /api/v1/expenses/{id} | Update expense |
| DELETE | /api/v1/expenses/{id} | Delete expense |
| GET | /api/v1/expenses/range | Get expenses by date range |

#### Create Expense

```json
POST /api/v1/expenses

{
  "description": "Lunch at cafe",
  "amount": 350.00,
  "expenseDate": "2026-07-10",
  "categoryId": 1
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "description": "Lunch at cafe",
  "amount": 350.00,
  "expenseDate": "2026-07-10",
  "categoryName": "Food",
  "budgetExceeded": false
}
```

#### Get Paginated Expenses

```
GET /api/v1/expenses/paged?page=0&size=10&sort=expenseDate,desc
```

Response:

```json
{
  "content": [...],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0
}
```

#### Get Expenses by Date Range

```
GET /api/v1/expenses/range?start=2026-07-01&end=2026-07-31
```

---

## Key Design Decisions

**JWT over sessions:** Stateless authentication scales horizontally without shared session storage. Every request carries its identity in the token.

**BigDecimal for money:** Float and double have precision errors that are unacceptable in financial calculations. BigDecimal is exact.

**DTOs everywhere:** JPA entities are never exposed directly in API responses. DTOs decouple the database schema from the API contract.

**Ownership checks on every query:** Every repository query filters by both ID and the current user — `findByIdAndUser`. This prevents any user from accessing another user's data even if they guess the ID.

**Budget aggregation at query time:** Monthly spend is calculated via a JPQL SUM query on each request rather than stored and updated. This keeps the data consistent and eliminates sync issues.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| SPRING_DATASOURCE_URL | jdbc:postgresql://localhost:5432/expenseflow | Database URL |
| SPRING_DATASOURCE_USERNAME | expenseuser | Database username |
| SPRING_DATASOURCE_PASSWORD | expensepass | Database password |
| JWT_SECRET | expenseflow-super-secret-key... | JWT signing secret |
| JWT_EXPIRATION | 86400000 | Token expiry in ms (24 hours) |

---

## CI Pipeline

GitHub Actions runs on every push to `master`:

1. Spins up PostgreSQL 16 as a service
2. Sets up Java 21 (Temurin)
3. Caches Maven dependencies
4. Runs `mvn clean install`

Status badge shows build health on every commit.
# Finance Data Processing and Access Control Backend

A Spring Boot REST API backend for a **Finance Dashboard System** with role-based access control (RBAC), JWT authentication, financial record management, and dashboard analytics.

---


## About This Project

This is a **Finance Data Processing and Access Control Backend** built as part of
the Zorvyn FinTech Backend Developer Internship Assignment.

It is a production-structured Spring Boot REST API that powers a Finance Dashboard
System where different users interact with financial records based on their role


### What it does:
- 🔐 **JWT Authentication** — Secure login with token-based auth
- 👥 **Role-Based Access Control** — ADMIN, ANALYST, VIEWER roles with different permissions
- 💰 **Financial Records Management** — Full CRUD with filters and pagination
- 📊 **Dashboard Analytics** — Total income, expenses, net balance, category-wise totals, monthly trends
- ✅ **Input Validation** — Proper error handling with meaningful messages
- 🗄️ **H2 In-Memory Database** — Easy to run locally without any DB setup

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Validation | Jakarta Validation (Bean Validation) |
| Build Tool | Maven |

> **Assumption:** H2 in-memory database is used for simplicity. To use MySQL/PostgreSQL, update `application.properties` with the appropriate datasource URL and driver, and add the corresponding dependency in `pom.xml`.

---

## Project Structure (Eclipse-ready Maven project)

```
finance-backend/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/zorvyn/finance/
    │   │   ├── FinanceBackendApplication.java      ← Main entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java             ← Spring Security + JWT filter chain
    │   │   │   └── DataSeeder.java                 ← Seeds default users & sample records
    │   │   ├── controller/
    │   │   │   ├── AuthController.java             ← /api/auth/login, /api/auth/register
    │   │   │   ├── UserController.java             ← /api/users, /api/profile
    │   │   │   ├── FinancialRecordController.java  ← /api/records (CRUD + filters)
    │   │   │   ├── DashboardController.java        ← /api/dashboard/summary
    │   │   │   └── AnalyticsController.java        ← /api/analytics/insights
    │   │   ├── dto/                                ← Request/Response DTOs
    │   │   ├── exception/                          ← Custom exceptions + GlobalExceptionHandler
    │   │   ├── model/                              ← JPA entities (User, FinancialRecord)
    │   │   ├── repository/                         ← Spring Data JPA repositories
    │   │   ├── security/                           ← JWT utils, filter, UserDetailsService
    │   │   └── service/                            ← Business logic layer
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/zorvyn/finance/
            └── FinanceBackendIntegrationTest.java  ← 9 integration tests
```

---

## How to Import into Eclipse IDE

1. Open Eclipse → **File → Import → Maven → Existing Maven Projects**
2. Browse to the `finance-backend/` folder → Click **Finish**
3. Wait for Maven to download all dependencies
4. Run `FinanceBackendApplication.java` as a **Java Application**
5. Server starts on **http://localhost:8080**

---

## Running the Application

```bash
# From the project root directory
mvn spring-boot:run

# Or build and run the JAR
mvn clean package -DskipTests
java -jar target/finance-backend-1.0.0.jar
```

---

## Default Test Users (seeded at startup)

| Role | Email | Password | Permissions |
|---|---|---|---|
| ADMIN | admin@zorvyn.com | admin123 | Full access: users, records, dashboard, analytics |
| ANALYST | analyst@zorvyn.com | analyst123 | Read records, dashboard, analytics/insights |
| VIEWER | viewer@zorvyn.com | viewer123 | Read records and dashboard only |

---

## Role Permission Matrix

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| POST /api/auth/login | ✅ | ✅ | ✅ |
| POST /api/auth/register | ✅ | ✅ | ✅ |
| GET /api/profile | ✅ | ✅ | ✅ |f
| GET /api/records | ✅ | ✅ | ✅ |
| GET /api/records/{id} | ✅ | ✅ | ✅ |
| POST /api/records | ❌ | ❌ | ✅ |
| PUT /api/records/{id} | ❌ | ❌ | ✅ |
| DELETE /api/records/{id} | ❌ | ❌ | ✅ |
| GET /api/dashboard/summary | ✅ | ✅ | ✅ |
| GET /api/analytics/insights | ❌ | ✅ | ✅ |
| GET /api/users | ❌ | ❌ | ✅ |
| GET /api/users/{id} | ❌ | ❌ | ✅ |
| PUT /api/users/{id} | ❌ | ❌ | ✅ |
| DELETE /api/users/{id} | ❌ | ❌ | ✅ |

---

## API Reference

### Authentication

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@zorvyn.com",
  "password": "admin123"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "type": "Bearer",
    "id": 1,
    "name": "Admin User",
    "email": "admin@zorvyn.com",
    "role": "ADMIN"
  }
}
```

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123",
  "role": "VIEWER"
}
```

---

### Financial Records

All requests require: `Authorization: Bearer <token>`

#### List Records (with filters + pagination)
```
GET /api/records?type=INCOME&category=Salary&startDate=2026-01-01&endDate=2026-12-31&page=0&size=10&sortBy=date&sortDir=desc
```

#### Get Record by ID
```
GET /api/records/1
```

#### Create Record (ADMIN only)
```
POST /api/records
Content-Type: application/json

{
  "amount": 50000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "notes": "Monthly salary"
}
```

#### Update Record (ADMIN only)
```
PUT /api/records/1
Content-Type: application/json

{
  "amount": 55000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "notes": "Updated salary"
}
```

#### Delete Record (ADMIN only, soft delete)
```
DELETE /api/records/1
```

---

### Dashboard

#### Summary
```
GET /api/dashboard/summary
```
**Response includes:**
- `totalIncome`, `totalExpenses`, `netBalance`
- `categoryWiseTotals` — map of category → amount
- `recentActivity` — last 10 records
- `monthlyTrends` — per-month income/expense/net breakdown

---

### Analytics (ANALYST + ADMIN)

```
GET /api/analytics/insights
```
Returns monthly trends and category-wise totals.

---

### User Management (ADMIN only)

```
GET    /api/users          → List all users
GET    /api/users/{id}     → Get user by ID
PUT    /api/users/{id}     → Update name, role, or active status
DELETE /api/users/{id}     → Soft-deactivate user
GET    /api/profile        → Get own profile (any role)
```

#### Update User
```
PUT /api/users/2
Content-Type: application/json

{
  "name": "Updated Name",
  "role": "ANALYST",
  "active": true
}
```

---

## Error Response Format

All errors follow a consistent structure:
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "amount": "Amount is required",
    "category": "Category is required"
  },
  "timestamp": "2026-04-05T10:30:00"
}
```

### HTTP Status Codes Used

| Code | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient role) |
| 404 | Resource Not Found |
| 500 | Internal Server Error |

---

## H2 Database Console

Available at: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: *(empty)*

---

## Running Tests

```bash
mvn test
```

9 integration tests covering:
- Login success / wrong credentials
- Admin creating records
- Viewer blocked from creating records
- Viewer can read records
- Dashboard access
- Unauthenticated request blocked
- Validation error on bad input
- Admin user management access
- Viewer blocked from user management

---

## Design Decisions & Assumptions

1. **H2 in-memory DB** — Used for simplicity. Production-ready by swapping datasource config.
2. **Soft deletes** — Financial records and users are never hard-deleted; a `deleted`/`active` flag is used.
3. **JWT stateless auth** — No server-side sessions; tokens expire after 24 hours.
4. **VIEWER role** — Can read all records and dashboard but cannot create/update/delete anything.
5. **ANALYST role** — VIEWER permissions + analytics/insights endpoint access.
6. **ADMIN role** — Full access including user management and all write operations.
7. **Pagination** — All list endpoints support pagination and sorting via query parameters.
8. **Consistent API response** — All endpoints return a standard `ApiResponse<T>` envelope.

---

## API Screenshots (Postman Testing)

### 1. Admin Login — 200 OK
![Admin Login](admin-login.png)

### 2. Dashboard Summary
![Dashboard](dashboard.png)

### 3. GET All Records
![Get Records](get-records.png)

### 4. POST Create Record (Admin)
![Create Record](create-record.png)

### 5. Viewer Login
![Viewer Login](viewer-login.png)

### 6. Viewer GET Records — 200 OK ✅
![Viewer Get Records](viewer-get-records.png)

### 7. Viewer POST Records — 403 Forbidden ❌
![Viewer 403 Create](viewer-403-create.png)

### 8. Viewer GET Analytics — 403 Forbidden ❌
![Viewer 403 Analytics](viewer-403-analytics.png)

### 9. Viewer GET Users — 403 Forbidden ❌
![Viewer 403 Users](viewer-403-users.png)

### 10. Analyst Login
![Analyst Login](analyst-login.png)

### 11. Analyst GET Analytics — 200 OK ✅
![Analyst Analytics](analyst-analytics.png)

### 12. Analyst POST Records — 403 Forbidden ❌
![Analyst 403 Create](analyst-403-create.png)

### 13. Admin GET All Users
![Admin Users](admin-users.png)

### 14. Validation Error — 400 Bad Request
![Validation Error](validation-error.png)
# PROJECT_SPEC.md

# Telecom CRM & Support Platform
## Enterprise Backend Engineering Specification

---

## Table of Contents

1. [AI Development Context](#ai-development-context)
2. [Project Vision](#project-vision)
3. [Technology Stack](#technology-stack)
4. [Required Dependencies](#required-dependencies)
5. [Architecture Philosophy](#architecture-philosophy)
6. [Package Structure](#package-structure)
7. [Coding Conventions](#coding-conventions)
8. [Security Requirements](#security-requirements)
9. [Database Design & ERD](#database-design--erd)
10. [API Standards & Endpoint Contracts](#api-standards--endpoint-contracts)
11. [DTO Schemas](#dto-schemas)
12. [Kafka Event Schemas](#kafka-event-schemas)
13. [Redis Cache Invalidation Flows](#redis-cache-invalidation-flows)
14. [Elasticsearch Index Mappings](#elasticsearch-index-mappings)
15. [Sequence Diagrams](#sequence-diagrams)
16. [Development Roadmap](#development-roadmap)
17. [Redis Strategy](#redis-strategy)
18. [Elasticsearch Strategy](#elasticsearch-strategy)
19. [Kafka Strategy](#kafka-strategy)
20. [Testing Strategy](#testing-strategy)
21. [Branching Strategy](#branching-strategy)
22. [Definition of Done](#definition-of-done)
23. [Future Enhancements](#future-enhancements)

---

## AI Development Context

You are acting as a Senior Backend Engineer helping build this project.

Responsibilities:
- Follow enterprise Java and Spring Boot best practices.
- Prefer maintainability over shortcuts.
- Keep controllers thin.
- Keep business logic inside services.
- Use DTOs instead of exposing entities.
- Follow SOLID principles.
- Generate tests for business-critical logic.
- Explain architectural decisions when implementing features.

When generating code, always:
- Reference this specification as the single source of truth.
- Respect module boundaries; never let one module directly access another module's repository.
- Use the exact field names, enum values, and exception class names defined in this document.
- Return `ApiResponse<T>` wrappers from all controllers.
- Follow the DTO schemas defined in the DTO Schemas section exactly.

---

## Project Vision

Build a telecom CRM and support platform capable of managing:

- Users
- Customers
- Subscription Plans
- Subscriptions
- Support Tickets
- Ticket Comments
- Analytics Dashboards

The application should start as a modular monolith but be designed for future migration to microservices.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build Tool | Maven |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate 6 |
| Security | Spring Security 6 + JWT (jjwt) |
| Cache | Redis 7 |
| Messaging | Apache Kafka 3 |
| Search | Elasticsearch 8 |
| API Docs | Springdoc OpenAPI 3 (Swagger UI) |
| Testing | JUnit 5 + Mockito |

| Utilities | Lombok, MapStruct |

---

## Required Dependencies

### Spring Initializr Selections

```xml
<!-- Core -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-security</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>spring-boot-starter-validation</dependency>

<!-- Database -->
<dependency>postgresql</dependency>

<!-- Developer Experience -->
<dependency>lombok</dependency>
<dependency>spring-boot-devtools</dependency>
```

### Additional pom.xml Dependencies

```xml
<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.3</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.3</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.3</version>
  <scope>runtime</scope>
</dependency>

<!-- API Documentation -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>

<!-- Kafka -->
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Elasticsearch -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>

<!-- Redis -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- MapStruct -->
<dependency>
  <groupId>org.mapstruct</groupId>
  <artifactId>mapstruct</artifactId>
  <version>1.5.5.Final</version>
</dependency>
<dependency>
  <groupId>org.mapstruct</groupId>
  <artifactId>mapstruct-processor</artifactId>
  <version>1.5.5.Final</version>
  <scope>provided</scope>
</dependency>
```

---

## Architecture Philosophy

The system is a **modular monolith**.

Each module is a self-contained vertical slice. Modules communicate only through well-defined interfaces (service interfaces or application events), never through direct repository access across module boundaries.

### Modules

| Module | Responsibility |
|---|---|
| `auth` | Registration, login, JWT issuance and validation |
| `user` | User CRUD, profile management |
| `subscription` | Plan and subscription lifecycle management |
| `ticket` | Support ticket and comment workflow |
| `analytics` | Dashboard metrics aggregation |
| `notification` | Kafka consumer; dispatches notifications |
| `common` | Shared utilities, base classes, global exception handling |

### Module Coupling Rules

- A module **may** depend on `common`.
- A module **may** call another module's **service interface** (never its repository).
- No circular dependencies between modules.
- Inter-module communication for async flows uses **Kafka events**.

### Migration Path to Microservices

Each module maps 1:1 to a future microservice. Kafka events serve as the future message bus. Shared DB tables will be split by module ownership when decomposing.

---

## Package Structure

```
src/
└── main/
    └── java/
        └── com/company/telecomcrm/
            ├── TelecomCrmApplication.java
            ├── common/
            │   ├── dto/
            │   │   └── ApiResponse.java
            │   ├── exception/
            │   │   ├── GlobalExceptionHandler.java
            │   │   ├── BusinessRuleException.java
            │   │   ├── ResourceNotFoundException.java
            │   │   └── UnauthorizedException.java
            │   └── util/
            │       └── SecurityUtils.java
            ├── auth/
            │   ├── controller/AuthController.java
            │   ├── service/AuthService.java
            │   ├── dto/
            │   │   ├── LoginRequest.java
            │   │   ├── LoginResponse.java
            │   │   └── RegisterRequest.java
            │   └── security/
            │       ├── JwtUtil.java
            │       ├── JwtAuthFilter.java
            │       └── SecurityConfig.java
            ├── user/
            │   ├── controller/UserController.java
            │   ├── service/UserService.java
            │   ├── repository/UserRepository.java
            │   ├── entity/User.java
            │   ├── dto/
            │   │   ├── UserRequest.java
            │   │   └── UserResponse.java
            │   ├── mapper/UserMapper.java
            │   └── exception/UserNotFoundException.java
            ├── subscription/
            │   ├── controller/
            │   │   ├── PlanController.java
            │   │   └── SubscriptionController.java
            │   ├── service/
            │   │   ├── PlanService.java
            │   │   └── SubscriptionService.java
            │   ├── repository/
            │   │   ├── PlanRepository.java
            │   │   └── SubscriptionRepository.java
            │   ├── entity/
            │   │   ├── Plan.java
            │   │   └── Subscription.java
            │   ├── dto/
            │   │   ├── PlanRequest.java
            │   │   ├── PlanResponse.java
            │   │   ├── SubscriptionRequest.java
            │   │   └── SubscriptionResponse.java
            │   ├── mapper/
            │   │   ├── PlanMapper.java
            │   │   └── SubscriptionMapper.java
            │   └── exception/
            │       ├── PlanNotFoundException.java
            │       └── SubscriptionNotFoundException.java
            ├── ticket/
            │   ├── controller/
            │   │   ├── TicketController.java
            │   │   └── TicketCommentController.java
            │   ├── service/
            │   │   ├── TicketService.java
            │   │   └── TicketCommentService.java
            │   ├── repository/
            │   │   ├── TicketRepository.java
            │   │   └── TicketCommentRepository.java
            │   ├── entity/
            │   │   ├── Ticket.java
            │   │   └── TicketComment.java
            │   ├── dto/
            │   │   ├── TicketRequest.java
            │   │   ├── TicketResponse.java
            │   │   ├── TicketCommentRequest.java
            │   │   └── TicketCommentResponse.java
            │   ├── mapper/
            │   │   ├── TicketMapper.java
            │   │   └── TicketCommentMapper.java
            │   └── exception/TicketNotFoundException.java
            ├── analytics/
            │   ├── controller/DashboardController.java
            │   ├── service/DashboardService.java
            │   └── dto/
            │       ├── AdminDashboardResponse.java
            │       ├── SubscriptionDashboardResponse.java
            │       └── AgentDashboardResponse.java
            └── notification/
                ├── service/NotificationService.java
                └── consumer/KafkaEventConsumer.java

src/
└── test/
    └── java/
        └── com/company/telecomcrm/
            ├── user/service/UserServiceTest.java
            ├── subscription/service/SubscriptionServiceTest.java
            ├── ticket/service/TicketServiceTest.java
            └── analytics/service/DashboardServiceTest.java
```

---

## Coding Conventions

### Naming

| Artifact | Convention | Example |
|---|---|---|
| Classes | PascalCase | `TicketService`, `UserMapper` |
| Methods | camelCase | `createTicket()`, `findById()` |
| Variables | camelCase | `ticketId`, `currentUser` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS` |
| Packages | lowercase | `com.company.telecomcrm.ticket` |
| Database tables | snake_case | `ticket_comments` |
| Database columns | snake_case | `created_at`, `assigned_agent_id` |
| Kafka topics | kebab-case | `ticket-created`, `subscription-activated` |
| Redis keys | colon-namespaced | `dashboard:admin`, `plan:popular` |
| Elasticsearch indexes | lowercase-plural | `tickets`, `users` |

### Controller Rules

- Controllers are thin. No business logic.
- Controllers accept request DTOs and return `ApiResponse<ResponseDTO>`.
- Controllers delegate entirely to the service layer.
- Annotate with `@RestController` and `@RequestMapping("/api/v1/...")`.
- Use `@PreAuthorize` for method-level security.

```java
// CORRECT
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<SubscriptionResponse>> create(
        @Valid @RequestBody SubscriptionRequest request) {
    SubscriptionResponse response = subscriptionService.create(request);
    return ResponseEntity.ok(ApiResponse.success("Subscription created", response));
}

// WRONG — business logic does not belong in a controller
@PostMapping
public ResponseEntity<?> create(@RequestBody SubscriptionRequest request) {
    if (subscriptionRepository.countActiveByUserId(request.getUserId()) > 0) { ... }
}
```

### Service Rules

- Services contain all business logic and validation.
- Services throw typed exceptions (`UserNotFoundException`, `BusinessRuleException`, etc.).
- Services never return entities; they return DTOs via mappers.
- Services are annotated with `@Service` and `@Transactional` where appropriate.

### Entity Rules

- Entities are annotated with `@Entity`, `@Table(name = "...")`.
- Use `@CreationTimestamp` and `@UpdateTimestamp` for audit fields.
- Never expose entities outside the module; use mappers to convert to DTOs.
- Use `@Enumerated(EnumType.STRING)` for all enum fields.

### Exception Handling

- `GlobalExceptionHandler` (`@RestControllerAdvice`) handles all exceptions.
- Every caught exception returns a standardised `ApiResponse` with `success: false`.
- Business rule violations throw `BusinessRuleException`.
- Missing resources throw module-specific `NotFoundException` subclasses.

### Lombok Usage

- Use `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities and DTOs.
- Do **not** use `@Data` on entities (hashCode/equals issues with JPA).
- Use `@RequiredArgsConstructor` on services for constructor injection.

### Formatting

- Indentation: 4 spaces (no tabs).
- Max line length: 120 characters.
- One blank line between methods.
- Imports: no wildcard imports.

---

## Security Requirements

### Authentication

- Stateless JWT-based authentication.
- Token included in `Authorization: Bearer <token>` header.
- Token expiry: **24 hours** (configurable via `app.jwt.expiration-ms`).
- Token signing algorithm: **HS256**.
- Secret key stored in environment variable `JWT_SECRET` (minimum 256-bit).

### Authorization Roles

| Role | Description |
|---|---|
| `ROLE_USER` | Standard subscriber |
| `ROLE_SUPPORT_AGENT` | Support team member |
| `ROLE_ADMIN` | Full platform access |

### Role Permission Matrix

| Action | USER | SUPPORT_AGENT | ADMIN |
|---|---|---|---|
| View own profile | ✅ | ✅ | ✅ |
| Update own profile | ✅ | ✅ | ✅ |
| View all users | ❌ | ❌ | ✅ |
| Delete user | ❌ | ❌ | ✅ |
| View own subscriptions | ✅ | ❌ | ✅ |
| Create subscription | ❌ | ❌ | ✅ |
| Suspend/Cancel subscription | ❌ | ❌ | ✅ |
| Create ticket | ✅ (ACTIVE sub only) | ❌ | ✅ |
| View own tickets | ✅ | ❌ | ✅ |
| View all tickets | ❌ | ✅ | ✅ |
| Assign ticket | ❌ | ✅ | ✅ |
| Resolve ticket | ❌ | ✅ | ✅ |
| Close ticket | ❌ | ❌ | ✅ |
| Add ticket comment | ✅ (own) | ✅ (assigned) | ✅ |
| View dashboard | ❌ | ✅ (agent view) | ✅ |

### Password Rules

- BCrypt hashing with strength factor **12**.
- Never log, store, or return plain-text passwords.
- Passwords must be at least **8 characters**, contain at least one uppercase letter, one digit, and one special character (enforced via `@Pattern`).

### Public Endpoints (no auth required)

```
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

---

## Database Design & ERD

### Entity Relationship Diagram

```
┌───────────────────┐         ┌─────────────────────┐
│       users       │         │        plans         │
├───────────────────┤         ├─────────────────────┤
│ id (PK)           │         │ id (PK)              │
│ first_name        │         │ name                 │
│ last_name         │         │ type (MOBILE/        │
│ email (UNIQUE)    │         │       INTERNET/TV)   │
│ password          │         │ monthly_price        │
│ role              │         │ description          │
│ created_at        │         └──────────┬──────────┘
│ updated_at        │                    │ 1
└────────┬──────────┘                    │
         │ 1                             │ N
         │                    ┌──────────▼──────────┐
         │ N                  │     subscriptions    │
         └────────────────────┤─────────────────────┤
                              │ id (PK)              │
                              │ status               │
                              │ start_date           │
                              │ end_date             │
                              │ user_id (FK→users)   │
                              │ plan_id (FK→plans)   │
                              └──────────────────────┘

┌───────────────────┐         ┌─────────────────────┐
│       users       │         │       users          │
│  (as customer)    │         │  (as agent)          │
└────────┬──────────┘         └──────────┬──────────┘
         │ 1                             │ 0..1
         │ N                             │ N
         └───────────────────────────────┤
                              ┌──────────▼──────────┐
                              │       tickets        │
                              ├─────────────────────┤
                              │ id (PK)              │
                              │ title                │
                              │ description          │
                              │ status               │
                              │ priority             │
                              │ customer_id (FK)     │
                              │ assigned_agent_id(FK)│
                              │ created_at           │
                              │ updated_at           │
                              └──────────┬──────────┘
                                         │ 1
                                         │ N
                              ┌──────────▼──────────┐
                              │   ticket_comments    │
                              ├─────────────────────┤
                              │ id (PK)              │
                              │ message              │
                              │ author_id (FK→users) │
                              │ ticket_id (FK)       │
                              │ created_at           │
                              └──────────────────────┘
```

### Table Definitions

#### `users`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PRIMARY KEY |
| `first_name` | `VARCHAR(100)` | NOT NULL |
| `last_name` | `VARCHAR(100)` | NOT NULL |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE |
| `password` | `VARCHAR(255)` | NOT NULL |
| `role` | `VARCHAR(30)` | NOT NULL (ROLE_USER / ROLE_SUPPORT_AGENT / ROLE_ADMIN) |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() |
| `updated_at` | `TIMESTAMP` | NOT NULL |

#### `plans`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PRIMARY KEY |
| `name` | `VARCHAR(150)` | NOT NULL, UNIQUE |
| `type` | `VARCHAR(30)` | NOT NULL (MOBILE / INTERNET / TV) |
| `monthly_price` | `NUMERIC(10,2)` | NOT NULL |
| `description` | `TEXT` | |

#### `subscriptions`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PRIMARY KEY |
| `status` | `VARCHAR(30)` | NOT NULL (ACTIVE / SUSPENDED / CANCELLED / EXPIRED) |
| `start_date` | `DATE` | NOT NULL |
| `end_date` | `DATE` | |
| `user_id` | `BIGINT` | NOT NULL, FK → users(id) |
| `plan_id` | `BIGINT` | NOT NULL, FK → plans(id) |

#### `tickets`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PRIMARY KEY |
| `title` | `VARCHAR(255)` | NOT NULL |
| `description` | `TEXT` | NOT NULL |
| `status` | `VARCHAR(30)` | NOT NULL (OPEN / IN_PROGRESS / RESOLVED / CLOSED) |
| `priority` | `VARCHAR(20)` | NOT NULL (LOW / MEDIUM / HIGH) |
| `customer_id` | `BIGINT` | NOT NULL, FK → users(id) |
| `assigned_agent_id` | `BIGINT` | FK → users(id), NULLABLE |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() |
| `updated_at` | `TIMESTAMP` | NOT NULL |

#### `ticket_comments`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PRIMARY KEY |
| `message` | `TEXT` | NOT NULL |
| `author_id` | `BIGINT` | NOT NULL, FK → users(id) |
| `ticket_id` | `BIGINT` | NOT NULL, FK → tickets(id) |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() |

### Business Rules Summary

- Email must be unique across the `users` table.
- A `CANCELLED` subscription can never transition to `ACTIVE`.
- Only a user with an `ACTIVE` subscription may create a ticket.
- Only `OPEN` tickets can be assigned to an agent (status transitions to `IN_PROGRESS`).
- `CLOSED` tickets are immutable; no field updates or new comments allowed.
- Only `ADMIN` can create subscriptions.

---

## API Standards & Endpoint Contracts

### Standard Response Envelope

```json
// Success
{
  "success": true,
  "message": "Operation completed",
  "data": { }
}

// Error
{
  "success": false,
  "message": "Descriptive error message",
  "data": null
}
```

### HTTP Status Code Conventions

| Scenario | HTTP Status |
|---|---|
| Successful GET / read | 200 OK |
| Successful POST / create | 201 Created |
| Successful update | 200 OK |
| Successful delete | 200 OK |
| Validation error | 400 Bad Request |
| Authentication failure | 401 Unauthorized |
| Authorization failure | 403 Forbidden |
| Resource not found | 404 Not Found |
| Business rule violation | 422 Unprocessable Entity |
| Server error | 500 Internal Server Error |

### Auth Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | ❌ | — | Register a new user |
| `POST` | `/api/v1/auth/login` | ❌ | — | Login and receive JWT |

#### POST /api/v1/auth/register

Request:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "Secure@123"
}
```
Response `201`:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "ROLE_USER",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

#### POST /api/v1/auth/login

Request:
```json
{
  "email": "john.doe@example.com",
  "password": "Secure@123"
}
```
Response `200`:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

---

### User Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/v1/users` | ✅ | ADMIN | List all users |
| `GET` | `/api/v1/users/{id}` | ✅ | ADMIN, self | Get user by ID |
| `PUT` | `/api/v1/users/{id}` | ✅ | ADMIN, self | Update user |
| `DELETE` | `/api/v1/users/{id}` | ✅ | ADMIN | Delete user |

#### GET /api/v1/users/{id} — Response `200`

```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "ROLE_USER",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

#### GET /api/v1/users — Response `200`

```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [ { ...UserResponse }, { ...UserResponse } ]
}
```

---

### Plan Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/v1/plans` | ✅ | ADMIN | Create a plan |
| `GET` | `/api/v1/plans` | ✅ | ALL | List all plans |
| `GET` | `/api/v1/plans/{id}` | ✅ | ALL | Get plan by ID |
| `PUT` | `/api/v1/plans/{id}` | ✅ | ADMIN | Update plan |
| `DELETE` | `/api/v1/plans/{id}` | ✅ | ADMIN | Delete plan |

---

### Subscription Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/v1/subscriptions` | ✅ | ADMIN | Create subscription |
| `GET` | `/api/v1/subscriptions` | ✅ | ADMIN | List all subscriptions |
| `GET` | `/api/v1/subscriptions/{id}` | ✅ | ADMIN, owner | Get subscription |
| `GET` | `/api/v1/subscriptions/user/{userId}` | ✅ | ADMIN, self | Get user's subscriptions |
| `PATCH` | `/api/v1/subscriptions/{id}/suspend` | ✅ | ADMIN | Suspend subscription |
| `PATCH` | `/api/v1/subscriptions/{id}/cancel` | ✅ | ADMIN | Cancel subscription |

#### POST /api/v1/subscriptions — Request

```json
{
  "userId": 1,
  "planId": 3,
  "startDate": "2024-02-01"
}
```

#### PATCH /api/v1/subscriptions/{id}/suspend — Response `200`

```json
{
  "success": true,
  "message": "Subscription suspended successfully",
  "data": {
    "id": 5,
    "status": "SUSPENDED",
    ...
  }
}
```

---

### Ticket Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/v1/tickets` | ✅ | USER | Create ticket |
| `GET` | `/api/v1/tickets` | ✅ | SUPPORT_AGENT, ADMIN | List all tickets |
| `GET` | `/api/v1/tickets/{id}` | ✅ | owner, SUPPORT_AGENT, ADMIN | Get ticket |
| `GET` | `/api/v1/tickets/my` | ✅ | USER | Get own tickets |
| `PATCH` | `/api/v1/tickets/{id}/assign` | ✅ | SUPPORT_AGENT, ADMIN | Assign ticket |
| `PATCH` | `/api/v1/tickets/{id}/resolve` | ✅ | SUPPORT_AGENT, ADMIN | Resolve ticket |
| `PATCH` | `/api/v1/tickets/{id}/close` | ✅ | ADMIN | Close ticket |

#### POST /api/v1/tickets — Request

```json
{
  "title": "Internet connection drops every morning",
  "description": "My connection drops between 7–9 AM consistently.",
  "priority": "HIGH"
}
```

#### PATCH /api/v1/tickets/{id}/assign — Request

```json
{
  "agentId": 12
}
```

---

### Ticket Comment Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/v1/tickets/{ticketId}/comments` | ✅ | owner, SUPPORT_AGENT, ADMIN | Add comment |
| `GET` | `/api/v1/tickets/{ticketId}/comments` | ✅ | owner, SUPPORT_AGENT, ADMIN | List comments |

---

### Analytics / Dashboard Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/v1/dashboard/admin` | ✅ | ADMIN | Admin metrics |
| `GET` | `/api/v1/dashboard/subscriptions` | ✅ | ADMIN | Subscription metrics |
| `GET` | `/api/v1/dashboard/tickets` | ✅ | ADMIN | Ticket metrics |
| `GET` | `/api/v1/dashboard/agent` | ✅ | SUPPORT_AGENT, ADMIN | Agent metrics |

---

### Search Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/v1/search/tickets?q={query}` | ✅ | SUPPORT_AGENT, ADMIN | Full-text ticket search |
| `GET` | `/api/v1/search/users?q={query}` | ✅ | ADMIN | Full-text user search |

---

## DTO Schemas

### Auth DTOs

```java
// RegisterRequest.java
public record RegisterRequest(
    @NotBlank @Size(min=2, max=100) String firstName,
    @NotBlank @Size(min=2, max=100) String lastName,
    @NotBlank @Email String email,
    @NotBlank @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must be at least 8 chars with uppercase, digit, and special character"
    ) String password
) {}

// LoginRequest.java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

// LoginResponse.java
public record LoginResponse(
    String token,
    String tokenType,   // always "Bearer"
    long expiresIn      // seconds
) {}
```

### User DTOs

```java
// UserRequest.java
public record UserRequest(
    @NotBlank @Size(min=2, max=100) String firstName,
    @NotBlank @Size(min=2, max=100) String lastName,
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "...") String password
) {}

// UserResponse.java
public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### Plan DTOs

```java
// PlanRequest.java
public record PlanRequest(
    @NotBlank @Size(max=150) String name,
    @NotNull PlanType type,           // MOBILE | INTERNET | TV
    @NotNull @DecimalMin("0.01") BigDecimal monthlyPrice,
    String description
) {}

// PlanResponse.java
public record PlanResponse(
    Long id,
    String name,
    String type,
    BigDecimal monthlyPrice,
    String description
) {}
```

### Subscription DTOs

```java
// SubscriptionRequest.java
public record SubscriptionRequest(
    @NotNull Long userId,
    @NotNull Long planId,
    @NotNull LocalDate startDate
) {}

// SubscriptionResponse.java
public record SubscriptionResponse(
    Long id,
    String status,        // ACTIVE | SUSPENDED | CANCELLED | EXPIRED
    LocalDate startDate,
    LocalDate endDate,
    UserResponse user,
    PlanResponse plan
) {}
```

### Ticket DTOs

```java
// TicketRequest.java
public record TicketRequest(
    @NotBlank @Size(min=5, max=255) String title,
    @NotBlank @Size(min=10) String description,
    @NotNull TicketPriority priority  // LOW | MEDIUM | HIGH
) {}

// TicketAssignRequest.java
public record TicketAssignRequest(
    @NotNull Long agentId
) {}

// TicketResponse.java
public record TicketResponse(
    Long id,
    String title,
    String description,
    String status,        // OPEN | IN_PROGRESS | RESOLVED | CLOSED
    String priority,
    UserResponse customer,
    UserResponse assignedAgent,   // nullable
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### TicketComment DTOs

```java
// TicketCommentRequest.java
public record TicketCommentRequest(
    @NotBlank @Size(min=1, max=2000) String message
) {}

// TicketCommentResponse.java
public record TicketCommentResponse(
    Long id,
    String message,
    UserResponse author,
    Long ticketId,
    LocalDateTime createdAt
) {}
```

### Dashboard DTOs

```java
// AdminDashboardResponse.java
public record AdminDashboardResponse(
    long totalUsers,
    long activeSubscriptions,
    long cancelledSubscriptions,
    long openTickets,
    long resolvedTickets
) {}

// SubscriptionDashboardResponse.java
public record SubscriptionDashboardResponse(
    List<PlanPopularityDTO> mostPopularPlans,
    Map<String, Long> monthlyGrowth,           // key: "YYYY-MM", value: count
    Map<String, Long> statusDistribution       // key: status name, value: count
) {}

// AgentDashboardResponse.java
public record AgentDashboardResponse(
    Long agentId,
    String agentName,
    long assignedTickets,
    long resolvedTickets,
    double averageResolutionTimeHours
) {}

// PlanPopularityDTO.java
public record PlanPopularityDTO(
    Long planId,
    String planName,
    long activeSubscriberCount
) {}
```

### Common Wrapper

```java
// ApiResponse.java
@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).data(null).build();
    }
}
```

---

## Kafka Event Schemas

### Topic Naming

| Event | Topic Name | Partitions | Replication Factor |
|---|---|---|---|
| Ticket created | `ticket-created` | 3 | 1 (dev) / 3 (prod) |
| Ticket resolved | `ticket-resolved` | 3 | 1 (dev) / 3 (prod) |
| Subscription activated | `subscription-activated` | 3 | 1 (dev) / 3 (prod) |

### Base Event Envelope

All events share this outer envelope:

```json
{
  "eventId": "uuid-v4",
  "eventType": "TICKET_CREATED",
  "timestamp": "2024-01-15T10:30:00Z",
  "payload": { }
}
```

### TicketCreatedEvent

```json
{
  "eventId": "3f7a2b1c-...",
  "eventType": "TICKET_CREATED",
  "timestamp": "2024-01-15T10:30:00Z",
  "payload": {
    "ticketId": 101,
    "title": "Internet drops every morning",
    "priority": "HIGH",
    "customerId": 5,
    "customerEmail": "john.doe@example.com"
  }
}
```

### TicketResolvedEvent

```json
{
  "eventId": "9c1d4e8f-...",
  "eventType": "TICKET_RESOLVED",
  "timestamp": "2024-01-16T14:00:00Z",
  "payload": {
    "ticketId": 101,
    "resolvedByAgentId": 12,
    "resolutionTimeMinutes": 1410,
    "customerId": 5,
    "customerEmail": "john.doe@example.com"
  }
}
```

### SubscriptionActivatedEvent

```json
{
  "eventId": "a7f3c2d1-...",
  "eventType": "SUBSCRIPTION_ACTIVATED",
  "timestamp": "2024-02-01T00:00:00Z",
  "payload": {
    "subscriptionId": 22,
    "userId": 5,
    "userEmail": "john.doe@example.com",
    "planId": 3,
    "planName": "Home Internet 100Mbps",
    "startDate": "2024-02-01"
  }
}
```

### Java Event Classes

```java
// BaseEvent.java
@Getter
@Builder
public class BaseEvent<T> {
    private String eventId;        // UUID.randomUUID().toString()
    private String eventType;
    private Instant timestamp;
    private T payload;
}

// TicketCreatedPayload.java
@Getter @Builder
public class TicketCreatedPayload {
    private Long ticketId;
    private String title;
    private String priority;
    private Long customerId;
    private String customerEmail;
}
```

### Producer Configuration

```java
@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "${spring.kafka.bootstrap-servers}");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### Consumer Configuration

```java
@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "${spring.kafka.bootstrap-servers}");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "telecom-crm-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.company.telecomcrm.*");
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

---

## Redis Cache Invalidation Flows

### Cache Key Naming

| Cache | Key Pattern | TTL |
|---|---|---|
| Admin dashboard | `dashboard:admin` | 5 minutes |
| Subscription dashboard | `dashboard:subscriptions` | 5 minutes |
| Ticket dashboard | `dashboard:tickets` | 5 minutes |
| Agent dashboard | `dashboard:agent:{agentId}` | 5 minutes |
| Popular plans | `plans:popular` | 10 minutes |

### Cache-Aside Pattern

All dashboard endpoints follow the cache-aside pattern:

```
1. Controller calls DashboardService.getAdminMetrics()
2. Service checks Redis: GET dashboard:admin
3. Cache HIT  → deserialize JSON → return DTO
4. Cache MISS → query PostgreSQL → serialize → SET dashboard:admin (TTL 300s) → return DTO
```

### Invalidation Triggers

| Triggering Action | Cache Keys to Evict |
|---|---|
| Ticket created | `dashboard:admin`, `dashboard:tickets` |
| Ticket status changed (resolve/close) | `dashboard:admin`, `dashboard:tickets`, `dashboard:agent:{agentId}` |
| Ticket assigned | `dashboard:agent:{agentId}` |
| Subscription created | `dashboard:admin`, `dashboard:subscriptions`, `plans:popular` |
| Subscription status changed | `dashboard:admin`, `dashboard:subscriptions` |

### Spring Cache Configuration

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())
            );
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfiguration())
            .build();
    }
}
```

### Cache Usage in Services

```java
// DashboardService.java
@Cacheable(value = "dashboard:admin", unless = "#result == null")
public AdminDashboardResponse getAdminMetrics() { ... }

// TicketService.java
@CacheEvict(value = {"dashboard:admin", "dashboard:tickets"}, allEntries = true)
public TicketResponse createTicket(TicketRequest request) { ... }

@CacheEvict(value = {"dashboard:admin", "dashboard:tickets"}, allEntries = true)
public TicketResponse resolveTicket(Long ticketId) { ... }
```

---

## Elasticsearch Index Mappings

### `tickets` Index

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "title": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "description": {
        "type": "text",
        "analyzer": "standard"
      },
      "status": { "type": "keyword" },
      "priority": { "type": "keyword" },
      "customerId": { "type": "long" },
      "customerEmail": { "type": "keyword" },
      "assignedAgentId": { "type": "long" },
      "createdAt": { "type": "date", "format": "strict_date_optional_time" },
      "updatedAt": { "type": "date", "format": "strict_date_optional_time" }
    }
  },
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

### `users` Index

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "firstName": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "lastName": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "email": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "role": { "type": "keyword" }
    }
  },
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

### Spring Data Elasticsearch Document Classes

```java
// TicketDocument.java
@Document(indexName = "tickets")
@Getter @Setter @Builder
public class TicketDocument {
    @Id private String id;
    @Field(type = FieldType.Text, analyzer = "standard") private String title;
    @Field(type = FieldType.Text, analyzer = "standard") private String description;
    @Field(type = FieldType.Keyword) private String status;
    @Field(type = FieldType.Keyword) private String priority;
    @Field(type = FieldType.Long) private Long customerId;
    @Field(type = FieldType.Keyword) private String customerEmail;
    @Field(type = FieldType.Long) private Long assignedAgentId;
    @Field(type = FieldType.Date, format = DateFormat.strict_date_optional_time)
    private LocalDateTime createdAt;
}

// UserDocument.java
@Document(indexName = "users")
@Getter @Setter @Builder
public class UserDocument {
    @Id private String id;
    @Field(type = FieldType.Text, analyzer = "standard") private String firstName;
    @Field(type = FieldType.Text, analyzer = "standard") private String lastName;
    @MultiField(mainField = @Field(type = FieldType.Text),
                otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) })
    private String email;
    @Field(type = FieldType.Keyword) private String role;
}
```

### Search Query Strategy

Full-text search uses a `multi_match` query across relevant text fields:

```java
// TicketSearchRepository.java
public interface TicketSearchRepository extends ElasticsearchRepository<TicketDocument, String> {}

// In TicketService.java
public List<TicketResponse> search(String query) {
    NativeQuery searchQuery = NativeQuery.builder()
        .withQuery(q -> q.multiMatch(m -> m
            .query(query)
            .fields("title", "description")
            .type(TextQueryType.BestFields)
            .fuzziness("AUTO")
        ))
        .build();

    SearchHits<TicketDocument> hits = elasticsearchTemplate.search(searchQuery, TicketDocument.class);
    return hits.getSearchHits().stream()
        .map(hit -> ticketMapper.documentToResponse(hit.getContent()))
        .collect(Collectors.toList());
}
```

---

## Sequence Diagrams

### 1. User Login & JWT Issuance

```
Client          AuthController       AuthService         UserRepository       JwtUtil
  │                   │                   │                     │                │
  │ POST /auth/login  │                   │                     │                │
  │──────────────────▶│                   │                     │                │
  │                   │ login(request)    │                     │                │
  │                   │──────────────────▶│                     │                │
  │                   │                   │ findByEmail(email)  │                │
  │                   │                   │────────────────────▶│                │
  │                   │                   │◀────────────────────│                │
  │                   │                   │                     │                │
  │                   │                   │ BCrypt.matches()    │                │
  │                   │                   │─────────────────────┘                │
  │                   │                   │                                      │
  │                   │                   │ generateToken(user)                  │
  │                   │                   │─────────────────────────────────────▶│
  │                   │                   │◀─────────────────────────────────────│
  │                   │                   │                                      │
  │                   │ LoginResponse(jwt)│                                      │
  │                   │◀──────────────────│                                      │
  │  200 { token }    │                   │                                      │
  │◀──────────────────│                   │                                      │
```

### 2. Create Ticket (Business Rule Enforcement)

```
Client         TicketController      TicketService      SubscriptionRepo     TicketRepo       Kafka
  │                  │                    │                    │                 │               │
  │POST /tickets     │                    │                    │                 │               │
  │─────────────────▶│                    │                    │                 │               │
  │                  │ createTicket(req)  │                    │                 │               │
  │                  │───────────────────▶│                    │                 │               │
  │                  │                    │ hasActiveSubscription(userId)        │               │
  │                  │                    │───────────────────▶│                 │               │
  │                  │                    │◀───────────────────│                 │               │
  │                  │                    │                    │                 │               │
  │                  │                    │ [if NO] throw BusinessRuleException  │               │
  │                  │                    │                                      │               │
  │                  │                    │ [if YES] save(ticket)                │               │
  │                  │                    │─────────────────────────────────────▶│               │
  │                  │                    │◀─────────────────────────────────────│               │
  │                  │                    │                                      │               │
  │                  │                    │ publish(TicketCreatedEvent)                          │
  │                  │                    │────────────────────────────────────────────────────▶│
  │                  │  TicketResponse    │                                                      │
  │                  │◀───────────────────│                                                      │
  │ 201 { ticket }   │                                                                           │
  │◀─────────────────│                                                                           │
```

### 3. Dashboard Request with Redis Cache

```
Client         DashboardController    DashboardService      Redis          PostgreSQL
  │                    │                    │                  │                │
  │GET /dashboard/admin│                    │                  │                │
  │───────────────────▶│                    │                  │                │
  │                    │ getAdminMetrics()  │                  │                │
  │                    │───────────────────▶│                  │                │
  │                    │                    │ GET dashboard:admin               │
  │                    │                    │─────────────────▶│                │
  │                    │                    │◀─────────────────│                │
  │                    │                    │                  │                │
  │                    │  [HIT] return DTO  │                  │                │
  │                    │                    │                  │                │
  │                    │  [MISS]            │                  │                │
  │                    │                    │ aggregate queries                  │
  │                    │                    │────────────────────────────────▶│
  │                    │                    │◀────────────────────────────────│
  │                    │                    │                  │                │
  │                    │                    │ SET dashboard:admin TTL=300       │
  │                    │                    │─────────────────▶│                │
  │                    │  return DTO        │                  │                │
  │                    │◀───────────────────│                  │                │
  │ 200 { dashboard }  │                    │                  │                │
  │◀───────────────────│                    │                  │                │
```

### 4. JWT Request Filter Flow

```
Client            JwtAuthFilter          JwtUtil          UserDetailsService    SecurityContext
  │                    │                    │                    │                     │
  │ GET /api/v1/...    │                    │                    │                     │
  │ Authorization:     │                    │                    │                     │
  │ Bearer <token>     │                    │                    │                     │
  │───────────────────▶│                    │                    │                     │
  │                    │ extractToken()     │                    │                     │
  │                    │───────────────────▶│                    │                     │
  │                    │ validateToken()    │                    │                     │
  │                    │───────────────────▶│                    │                     │
  │                    │◀───────────────────│                    │                     │
  │                    │                    │                    │                     │
  │                    │ [valid] loadUserByUsername(email)       │                     │
  │                    │────────────────────────────────────────▶│                     │
  │                    │◀────────────────────────────────────────│                     │
  │                    │                    │                    │                     │
  │                    │ setAuthentication(token)                                      │
  │                    │──────────────────────────────────────────────────────────────▶│
  │                    │                    │                    │                     │
  │                    │ chain.doFilter() → Controller                                 │
```

---

## Development Roadmap

### Phase 1 — Project Setup

**Goals:**
- Configure PostgreSQL with `application.yml` and profile-specific `application-local.yml`.
- Create full package structure as defined in the Package Structure section.
- Configure Hibernate DDL auto (use `validate` in prod, `create-drop` in test).

**Acceptance Criteria:**
- Application starts successfully on all profiles.
- Database connection works and tables are created.
- All packages exist even if empty.

---

### Phase 2 — User Management

**Features:**
- Create User (`POST /api/v1/users` — ADMIN only)
- Get User by ID (`GET /api/v1/users/{id}`)
- Update User (`PUT /api/v1/users/{id}`)
- Delete User (`DELETE /api/v1/users/{id}`)

**Acceptance Criteria:**
- CRUD operations functional and return `ApiResponse<UserResponse>`.
- Validation annotations present on `UserRequest`.
- `GlobalExceptionHandler` returns `400` on constraint violations.
- `UserNotFoundException` returns `404`.

---

### Phase 3 — Authentication

**Features:**
- Register (`POST /api/v1/auth/register`)
- Login (`POST /api/v1/auth/login`)
- JWT generation on login.
- `JwtAuthFilter` validates token on each protected request.

**Acceptance Criteria:**
- Protected endpoints return `401` without a valid token.
- Login returns a `Bearer` token with correct expiry.
- Token includes `sub` (email) and `role` claims.

---

### Phase 4 — Authorization

**Role Enforcement:**

| Role | Permitted Actions |
|---|---|
| `ROLE_USER` | View own profile, view own subscriptions, create tickets, comment on own tickets |
| `ROLE_SUPPORT_AGENT` | View all tickets, assign/resolve tickets, comment on assigned tickets |
| `ROLE_ADMIN` | Full access to all endpoints |

**Acceptance Criteria:**
- Accessing an endpoint with insufficient role returns `403`.
- `@PreAuthorize` annotations present on all restricted endpoints.
- A `ROLE_USER` cannot access `/api/v1/users` (admin list).

---

### Phase 5 — Subscription Management

**Features:**
- Create subscription (ADMIN)
- Suspend subscription (`PATCH /{id}/suspend` — ADMIN)
- Cancel subscription (`PATCH /{id}/cancel` — ADMIN)
- View subscriptions by user

**Business Rules Enforced:**
- `CANCELLED` → `ACTIVE` transition must throw `BusinessRuleException`.
- Only one subscription per user per plan allowed simultaneously (optional constraint; document if deferred).

**Acceptance Criteria:**
- Subscription lifecycle transitions work correctly.
- Invalid transitions return `422`.

---

### Phase 6 — Ticket Management

**Features:**
- Create ticket (authenticated USER with ACTIVE subscription)
- Assign ticket to agent (SUPPORT_AGENT / ADMIN)
- Resolve ticket (SUPPORT_AGENT / ADMIN)
- Close ticket (ADMIN)

**Business Rules Enforced:**
- Non-ACTIVE subscriber creating a ticket → `422 BusinessRuleException`.
- Assigning a non-OPEN ticket → `422`.
- Modifying a CLOSED ticket → `422`.

**Acceptance Criteria:**
- All ticket state transitions enforced.
- Kafka `TicketCreatedEvent` published on creation.
- Kafka `TicketResolvedEvent` published on resolution.

---

### Phase 7 — Ticket Comments

**Features:**
- Add comment to ticket
- Retrieve all comments for a ticket

**Rules:**
- Comments are not allowed on CLOSED tickets.
- Only ticket owner, assigned agent, or ADMIN may comment.

**Acceptance Criteria:**
- Comment on CLOSED ticket returns `422`.
- Unauthorized comment attempt returns `403`.

---

### Phase 8 — DTO Layer

**Requirements:**
- No entity class is returned from any controller or service method.
- Every request is received as a request DTO.
- Every response is sent as a response DTO.
- MapStruct mappers are used for all entity ↔ DTO conversions.

**Acceptance Criteria:**
- Zero `@Entity`-annotated classes appear in controller method signatures.
- All DTOs match the schemas defined in the DTO Schemas section.

---

### Phase 9 — Validation & Exceptions

**Validation Annotations Used:**

| Annotation | Applied To |
|---|---|
| `@NotBlank` | String fields that must be non-empty |
| `@NotNull` | Object/enum fields that are required |
| `@Email` | Email fields |
| `@Size(min, max)` | String length bounds |
| `@DecimalMin` | Price fields |
| `@Pattern` | Password complexity |

**Custom Exception Classes:**

| Exception | HTTP Status | Scenario |
|---|---|---|
| `UserNotFoundException` | 404 | User ID not found |
| `TicketNotFoundException` | 404 | Ticket ID not found |
| `SubscriptionNotFoundException` | 404 | Subscription ID not found |
| `PlanNotFoundException` | 404 | Plan ID not found |
| `BusinessRuleException` | 422 | Business constraint violated |
| `UnauthorizedException` | 403 | Insufficient permissions |

**Acceptance Criteria:**
- All validation errors return structured `ApiResponse` with `success: false`.
- `GlobalExceptionHandler` handles all exception types consistently.
- No raw `500` errors leak for handled exceptions.

---

### Phase 10 — Dashboard Analytics

**Admin Metrics** (`GET /api/v1/dashboard/admin`):
- Total users
- Active subscriptions
- Cancelled subscriptions
- Open tickets
- Resolved tickets

**Subscription Metrics** (`GET /api/v1/dashboard/subscriptions`):
- Most popular plans (by active subscriber count)
- Monthly subscription growth (last 12 months)
- Subscription status distribution

**Agent Metrics** (`GET /api/v1/dashboard/agent`):
- Assigned tickets count
- Resolved tickets count
- Average resolution time (in hours)

**Acceptance Criteria:**
- All three endpoints return correct aggregated data.
- Each endpoint is ADMIN-only (agent endpoint also accessible by SUPPORT_AGENT).

---

### Phase 11 — Redis Caching

**Cache Targets:**

| Endpoint | Cache Key | TTL |
|---|---|---|
| `GET /dashboard/admin` | `dashboard:admin` | 5 min |
| `GET /dashboard/subscriptions` | `dashboard:subscriptions` | 5 min |
| `GET /dashboard/tickets` | `dashboard:tickets` | 5 min |
| `GET /dashboard/agent` | `dashboard:agent:{agentId}` | 5 min |

**Acceptance Criteria:**
- Second request to a dashboard endpoint returns cached result (verifiable via logs showing no DB query).
- Cache evicts correctly when a ticket or subscription changes state.

---

### Phase 12 — Elasticsearch

**Indexed Entities:** Tickets, Users

**Synchronisation Strategy:** Dual-write — on entity save, persist to PostgreSQL and index to Elasticsearch.

**Search Endpoints:**
- `GET /api/v1/search/tickets?q=` — searches `title` and `description`.
- `GET /api/v1/search/users?q=` — searches `firstName`, `lastName`, and `email`.

**Acceptance Criteria:**
- Partial and fuzzy text search works on ticket title/description.
- New tickets and users are indexed within the same transaction.

---

### Phase 13 — Kafka

**Producers:** `TicketService` and `SubscriptionService` emit events.

**Consumers:** `KafkaEventConsumer` in the `notification` module subscribes to all topics.

**Events Produced:**
- `TicketCreatedEvent` on ticket creation.
- `TicketResolvedEvent` on ticket resolution.
- `SubscriptionActivatedEvent` on subscription creation.

**Acceptance Criteria:**
- Events visible in Kafka topic using `kafka-console-consumer`.
- Consumer logs each received event.
- Analytics module processes `TicketResolvedEvent` to update resolution time.

---

### Phase 14 — Unit Testing

**Test Classes Required:**

| Test Class | Coverage Focus |
|---|---|
| `UserServiceTest` | CRUD, duplicate email, not-found |
| `SubscriptionServiceTest` | Lifecycle transitions, cancelled reactivation guard |
| `TicketServiceTest` | Active subscriber guard, OPEN-only assignment, CLOSED immutability |
| `DashboardServiceTest` | Correct metric aggregation, Redis interaction |

**Testing Conventions:**
- Use `@ExtendWith(MockitoExtension.class)`.
- Mock all repositories and external dependencies.
- Name tests with the pattern: `methodName_scenario_expectedResult`.
- Every business rule must have at least one passing and one failing test case.

**Acceptance Criteria:**
- All service tests pass.
- No tests access a real database (all mocked).

---

### Phase 15 — Swagger / OpenAPI

**Requirements:**
- All endpoints documented with `@Operation`, `@ApiResponse`, and `@Parameter`.
- All DTOs documented with `@Schema`.
- JWT bearer auth configured in Swagger UI (padlock icon).
- Swagger UI accessible at `/swagger-ui.html`.

**Acceptance Criteria:**
- All endpoints visible and callable from Swagger UI.
- Auth lock present and token accepted via Swagger.

---

---

## Redis Strategy

### What to Cache

| Data | Rationale |
|---|---|
| Dashboard metrics | Expensive multi-table aggregation; changes infrequently |
| Popular plans | Low churn; queried on every subscription creation flow |

### What NOT to Cache

| Data | Rationale |
|---|---|
| Individual user profiles | Mutates frequently; cache consistency overhead not worth it |
| Ticket details | Real-time accuracy required for support agents |
| Auth tokens | Stateless JWT; Redis not needed for validation |

### Invalidation Strategy

- **Event-driven invalidation**: Services use `@CacheEvict` on write methods.
- **TTL fallback**: All keys expire after 5 minutes even without explicit eviction.
- **No stale reads acceptable for**: ticket status, subscription status (do not cache these).

---

## Elasticsearch Strategy

### Indexes

| Index | Documents | Primary Search Fields |
|---|---|---|
| `tickets` | `TicketDocument` | `title`, `description` |
| `users` | `UserDocument` | `firstName`, `lastName`, `email` |

### Write Strategy

Dual-write pattern: every entity save is followed by an index update. If Elasticsearch is unavailable, the PostgreSQL save should still succeed (Elasticsearch is search-only, not the source of truth).

### Search Features Supported

- Full-text keyword search
- Partial match (prefix)
- Fuzzy matching (typo tolerance via `fuzziness: AUTO`)

---

## Kafka Strategy

### Topics

| Topic | Producer | Consumers |
|---|---|---|
| `ticket-created` | `TicketService` | `NotificationService`, `DashboardService` |
| `ticket-resolved` | `TicketService` | `NotificationService`, `DashboardService` |
| `subscription-activated` | `SubscriptionService` | `NotificationService` |

### Consumer Group

All consumers share the group ID `telecom-crm-group` to ensure each event is processed once across instances.

### Error Handling

- Failed message processing retries up to **3 times** (configured via `spring.kafka.consumer.max-poll-records` and a `SeekToCurrentErrorHandler`).
- After max retries, message is sent to a dead-letter topic: `{original-topic}.DLT`.

---

## Testing Strategy

### Scope and Focus

| Layer | Approach |
|---|---|
| Service | Full unit tests with mocked repositories (primary coverage target) |
| Controller | Integration tests for auth and critical flows only; avoid duplicating service-layer tests |
| Repository | Not unit-tested; covered by integration tests if time permits |

### Test Structure

```
src/test/java/com/company/telecomcrm/
├── user/service/UserServiceTest.java
├── subscription/service/SubscriptionServiceTest.java
├── ticket/service/TicketServiceTest.java
└── analytics/service/DashboardServiceTest.java
```

### Naming Convention

```java
// Pattern: methodName_scenario_expectedOutcome
@Test
void createTicket_whenUserHasNoActiveSubscription_throwsBusinessRuleException() { ... }

@Test
void createTicket_whenUserHasActiveSubscription_returnsTicketResponse() { ... }

@Test
void cancelSubscription_whenAlreadyCancelled_throwsBusinessRuleException() { ... }
```

### Every Business Rule Must Have Two Tests

1. The **happy path** — rule satisfied, operation succeeds.
2. The **failure path** — rule violated, correct exception thrown.

---

## Branching Strategy

### Branch Model: GitHub Flow (simplified)

```
main
  └── feature/phase-1-project-setup
  └── feature/phase-2-user-management
  └── feature/phase-3-authentication
  └── bugfix/ticket-assignment-validation
  └── hotfix/jwt-expiry-config
```

### Branch Naming Conventions

| Prefix | Usage | Example |
|---|---|---|
| `feature/` | New feature or phase | `feature/phase-5-subscriptions` |
| `bugfix/` | Non-critical bug fix | `bugfix/dashboard-null-pointer` |
| `hotfix/` | Critical production fix | `hotfix/security-jwt-secret` |
| `chore/` | Non-functional changes | `chore/update-dependencies` |
| `refactor/` | Code improvement without behaviour change | `refactor/ticket-service-cleanup` |

### Rules

- `main` is always deployable. Direct commits to `main` are not allowed.
- All changes enter `main` via Pull Request.
- PRs require at least one passing CI run before merge.
- PRs should be scoped to a single phase or logical unit of work.
- Squash merge preferred to keep `main` history clean.

### Commit Message Convention (Conventional Commits)

```
<type>(<scope>): <short description>

Types: feat | fix | docs | style | refactor | test | chore
Scope: auth | user | subscription | ticket | analytics | notification | common

Examples:
feat(ticket): add business rule for active subscription check
fix(auth): correct JWT expiry calculation
test(subscription): add cancelled reactivation guard test
chore(deps): update elasticsearch client to 8.11.0
```

---

## Definition of Done

A feature is considered **complete** only when all of the following are true:

- [ ] Code compiles with no errors or warnings.
- [ ] All validation annotations are present on request DTOs.
- [ ] Authorization (`@PreAuthorize`) is enforced on all restricted endpoints.
- [ ] Business rules are implemented and verified in service layer.
- [ ] Custom exceptions are thrown for all error scenarios.
- [ ] `GlobalExceptionHandler` handles the new exception type.
- [ ] DTOs are used; no entity is exposed from any controller or service.
- [ ] MapStruct mapper is implemented for entity ↔ DTO conversion.
- [ ] Unit tests cover the happy path and all failure paths.
- [ ] Swagger `@Operation` and `@ApiResponse` annotations are present.
- [ ] No hardcoded values; all config in `application.yml` or environment variables.
- [ ] Redis cache is invalidated when relevant state changes.
- [ ] Kafka event is published when required by the event schema.
- [ ] Elasticsearch document is indexed/updated when entity is persisted.

---

## Future Enhancements

| Feature | Notes |
|---|---|
| Dockerization | Containerize the app and all services (PostgreSQL, Redis, Kafka, Elasticsearch) with Docker Compose for portable local and production deployments |
| Email Notifications | Send emails on ticket creation/resolution via SendGrid or SES |
| Audit Logging | Track all entity mutations with actor, timestamp, and before/after values |
| Rate Limiting | Protect public endpoints with Bucket4j or API Gateway rate limits |
| API Gateway | Spring Cloud Gateway as single entry point; route per module |
| Prometheus Monitoring | Expose `/actuator/prometheus`; scrape with Prometheus |
| Grafana Dashboards | Visualise JVM metrics, request latency, Kafka lag |
| Microservice Migration | Each module becomes an independent service; Kafka replaces in-process calls |
| Customer Entity | Separate `Customer` entity from `User` for richer CRM profile data |
| SLA Tracking | Auto-escalate HIGH priority tickets breaching response SLA |
| Multi-tenancy | Support multiple telecom operators on the same platform |

# Library Management API

A Spring Boot REST API for managing a small library system — authors, books, members, and borrowing records.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5.x |
| Language | Java 17 |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Spring Boot Validation |
| Documentation | springdoc-openapi + OpenAPI 3.0 |
| Build Tool | Maven |
| Tests | JUnit 5 + Mockito |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/library/
│   │   ├── config/          # OpenAPI config, DataSeeder
│   │   ├── controller/      # REST controllers
│   │   ├── dto/
│   │   │   ├── request/     # Incoming request DTOs
│   │   │   └── response/    # Outgoing response DTOs
│   │   ├── entity/          # JPA entities + BorrowStatus enum
│   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   │   ├── repository/      # Spring Data JPA repositories
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/library/service/   # Unit tests
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- (Optional) Docker Desktop — only if running via container

---

## Setup & Run

### Option 1 — Run with Maven

```bash
# Clone the repository
git clone https://github.com/<your-username>/library-management.git
cd library-management

# Run the application
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

Application starts at: `http://localhost:8080`

---

### Option 2 — Run with Docker

```bash
# Build the Docker image
docker build -t library-management .

# Run the container
docker run -p 8080:8080 library-management
```

Application starts at: `http://localhost:8080`

---

## Configuration

All business rules are externalised in `application.properties` — nothing is hardcoded.

| Property | Default | Description |
|---|---|---|
| `app.name` | Library Management API | Application name |
| `app.version` | 1.0.0 | Application version |
| `library.loan-period-days` | 14 | Days before a borrowed book is due |
| `library.max-active-borrows-per-member` | 3 | Maximum books a member can borrow at once |

---

## API Documentation

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON spec
```
http://localhost:8080/v3/api-docs
```

### OpenAPI YAML spec
```
http://localhost:8080/v3/api-docs.yaml
```

The `openapi.yaml` file is also available at the project root.

---

## H2 Console

The in-memory H2 database console is accessible at:

```
http://localhost:8080/h2-console
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:librarydb` |
| Username | `sa` |
| Password | _(leave empty)_ |

> **Sample Data:** On startup, the application automatically seeds the database with 3 authors,
> 3 books, and 2 members so you can test all endpoints immediately without manual setup.
---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/info` | Application name and version |
| `POST` | `/api/authors` | Create an author |
| `GET` | `/api/authors` | List all authors (paginated) |
| `GET` | `/api/authors/{id}` | Get author by ID |
| `PUT` | `/api/authors/{id}` | Update author |
| `POST` | `/api/books` | Add a book |
| `GET` | `/api/books` | List all books (paginated, filter by availability) |
| `GET` | `/api/books/{id}` | Get book by ID |
| `PUT` | `/api/books/{id}` | Update book |
| `DELETE` | `/api/books/{id}` | Delete a book |
| `POST` | `/api/members` | Register a member |
| `GET` | `/api/members` | List all members (paginated) |
| `GET` | `/api/members/{id}` | Get member by ID |
| `PUT` | `/api/members/{id}` | Update member |
| `POST` | `/api/borrows` | Borrow a book |
| `PUT` | `/api/borrows/{id}/return` | Return a borrowed book |
| `GET` | `/api/members/{id}/borrows` | Get borrow history for a member |

---

## Sample curl Requests

### App Info
```bash
curl http://localhost:8080/api/info
```

---

### Authors

**Create an author**
```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{"name": "Robert Martin", "email": "martin@books.com"}'
```

**List all authors (paginated)**
```bash
curl "http://localhost:8080/api/authors?page=0&size=5&sort=name,asc"
```

**Get author by ID**
```bash
curl http://localhost:8080/api/authors/1
```

**Update author**
```bash
curl -X PUT http://localhost:8080/api/authors/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Robert C. Martin", "email": "martin@books.com"}'
```

---

### Books

**Add a book**
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title": "Clean Code", "isbn": "978-01", "authorId": 1}'
```

**List all books**
```bash
curl "http://localhost:8080/api/books?page=0&size=5"
```

**List available books only**
```bash
curl "http://localhost:8080/api/books?available=true&page=0&size=5"
```

**Get book by ID**
```bash
curl http://localhost:8080/api/books/1
```

**Update book**
```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Clean Code", "isbn": "978-01", "authorId": 1}'
```

**Delete a book**
```bash
curl -X DELETE http://localhost:8080/api/books/1
```

---

### Members

**Register a member**
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@library.com", "phone": "9999999999"}'
```

**List all members**
```bash
curl "http://localhost:8080/api/members?page=0&size=5"
```

**Get member by ID**
```bash
curl http://localhost:8080/api/members/1
```

**Update member**
```bash
curl -X PUT http://localhost:8080/api/members/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Smith", "email": "alice@library.com", "phone": "8888888888"}'
```

---

### Borrowing

**Borrow a book**
```bash
curl -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "memberId": 1}'
```

**Return a book**
```bash
curl -X PUT http://localhost:8080/api/borrows/1/return
```

**Get borrow history for a member**
```bash
curl http://localhost:8080/api/members/1/borrows
```

---

## Business Rules

- A book cannot be borrowed if `available` is `false`
- A member cannot have more than `library.max-active-borrows-per-member` active borrows at once (default: 3)
- `dueDate` is automatically calculated as `borrowedAt + library.loan-period-days` (default: 14 days)
- Returning a book after the due date marks the record as `OVERDUE`
- Returning a book flips its `available` flag back to `true`

---

## Error Responses

All errors return a consistent structured JSON response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Author not found with id: 99",
  "path": "/api/authors/99",
  "timestamp": "2026-05-07T10:30:00"
}
```

Validation errors include a field-level breakdown:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/members",
  "timestamp": "2026-05-07T10:30:00",
  "validationErrors": {
    "email": "Invalid email format",
    "name": "Name is required"
  }
}
```

---

## Running Tests

```bash
./mvnw test
```

Unit tests cover the full service layer using JUnit 5 and Mockito — no Spring context is started, no database is involved.

# Budget Manager

Expense tracking system — Spring Boot REST API.

## Lab Works

### Lab 1 — Basic REST Service
- Spring Boot application
- REST API for **Expense** entity
- GET with `@PathVariable` and `@RequestParam`
- Layers: Controller → Service → Repository
- DTO + Mapper (MapStruct/Custom)
- Checkstyle

### Lab 2 — JPA (Hibernate / Spring Data)
- PostgreSQL via Docker Compose
- Entities and relationships:
  - **Many-to-One**: Expense → Category
  - **One-to-Many**: Category → Expenses
  - **Many-to-One**: Budget → Category (optional)
- CRUD for all entities
- N+1 solution (`@EntityGraph`, `JOIN FETCH`)
- Transaction demo (`@Transactional`, rollback)

## Entities

### Expense
| Field | Type | Description |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| amount | BigDecimal | Amount |
| date | LocalDate | Expense date |
| description | String | Description |
| *relations* | | Category |

### Other entities
- **Category**: Expense category (Food, Transport, etc.)
- **Budget**: Spending limit per category/period

## Run

### Requirements
- Docker & Docker Compose
- Java 17+
- Maven

### Quick start (Docker)
```bash
docker-compose up --build

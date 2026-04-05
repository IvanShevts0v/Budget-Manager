# Менеджер бюджета (Budget Manager) — REST API

Проект представляет собой Spring Boot REST API для учёта расходов. Приложение хранит записи о расходах в PostgreSQL и позволяет получать их через HTTP-эндпоинты.

## Возможности

- **Пользователи:** `GET /users/all`, `GET /users/{id}`, `POST /users/register` (создаёт пользователя и кошелёк по умолчанию), `PATCH /users/{id}/change-user-information`, `DELETE /users/{id}`.
- **Кошельки:** `GET /wallets`, `GET /wallets?userId=`, `GET /wallets/{id}`, `POST /wallets`, `PATCH /wallets/{id}/name/{name}`, `DELETE /wallets/{id}`.
- **Категории и теги:** CRUD по путям `/categories` и `/tags`; ответы категорий и тегов — DTO `NamedResponseDto` (`id`, `name`). Создание категории: `POST /categories` с телом `{"name":"..."}` (`CategoryRequestDto`).
- **Расходы:** `GET /expenses` (опционально `senderUserId` или фильтры `id`, `description`, `amount`, `category`, `date`), `GET /expenses/{id}`, `POST /expenses`, `POST /expenses/no-transactional` (два шага сохранения без внешней транзакции — демонстрация частичного коммита при ошибке на тегах), `PUT /expenses/{id}`, `DELETE /expenses/{id}`.
- Слои **Controller → Service → Repository**, DTO в виде JavaBean, маппинг **MapStruct** (`mapper` + сущности в `model.entity`).

В доменной модели есть связи **OneToMany** (`User` → `Wallet`, `Wallet` → `Expense`, `Category` → `Expense`) и **ManyToMany** (`Expense` ↔ `Tag` через `expense_tags`).

## Технологии

- Java 21
- Spring Boot
- Spring Web, Spring Data JPA
- MapStruct
- PostgreSQL
- Maven

## Модель данных

- **`User`** — пользователь (`username`); кошельки **OneToMany** → `Wallet`.
- **`Wallet`** — кошелёк; владелец **ManyToOne** ← `User`; расходы **OneToMany** → `Expense`.
- **`Category`**, **`Tag`** — справочники (наследник `AbstractNamedEntity` с полем `name`).
- **`Expense`** — расход: кошелёк, категория, сумма, дата, описание, теги.

В ответе API поле `category` — **имя** категории; также `walletId`, `userId`, список имён тегов `tags`.

### FetchType и CascadeType

| Связь | FetchType | CascadeType |
|-------|-----------|-------------|
| `User` → `Wallet` | `LAZY` | `PERSIST`, `MERGE`, `REMOVE`, `orphanRemoval` |
| `Wallet` → `Expense` | `LAZY` | `PERSIST`, `MERGE`, `REMOVE`, `orphanRemoval` |
| `Expense` → `Wallet`, `Category` | `LAZY` | нет |
| `Expense` ↔ `Tag` | `LAZY` | нет |

Для списков расходов в `ExpenseRepository` задан **`@EntityGraph`**, фильтры — в [`ExpenseSpecifications`](src/main/java/app/budgetmanager/repository/ExpenseSpecifications.java).

## API

Регистрация (`POST /users/register`):

```json
{
  "username": "ivan",
  "defaultWalletName": "Основной"
}
```

Поле `defaultWalletName` необязательно (по умолчанию имя кошелька — `Default`). В ответе — `id`, `username`, `walletIds`.

Тело для `POST`/`PUT` расхода:

```json
{
  "description": "Coffee",
  "amount": 150.00,
  "date": "2026-03-01",
  "walletId": 1,
  "categoryId": 1,
  "tagIds": [1, 2]
}
```

`amount` должно быть **строго больше нуля**. `tagIds` можно опустить или передать `[]`.

**Данные в PostgreSQL** между перезапусками сохраняются; при пустой БД сначала создайте пользователя (`/users/register`), категорию (`POST /categories`) и при необходимости теги.

## Качество кода

- Checkstyle (`checkstyle.xml`), проверяется только `src/main/java` (без сгенерированных MapStruct-реализаций).
- SonarCloud: https://sonarcloud.io/project/overview?id=IvanShevts0v_Budget-Manager

## Настройка и окружение

В `application.yml`: PostgreSQL `budgetdb`, `spring.jpa.hibernate.ddl-auto=update`, `server.port=8080`, пароль `${dbpassword}`.

## Сборка и запуск

```bash
mvn spring-boot:run
```

```bash
mvn clean install
mvn checkstyle:check
```

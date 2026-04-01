# Менеджер бюджета (Budget Manager) — REST API

Проект представляет собой Spring Boot REST API для учёта расходов. Приложение хранит записи о расходах в PostgreSQL и позволяет получать их через HTTP-эндпоинты.

## Возможности

- Просмотр списка расходов с фильтрацией по любым полям сущности `Expense`:
  - `GET /api/expenses?id=&description=&amount=&category=&date=YYYY-MM-DD`
- Получение расхода по идентификатору:
  - `GET /api/expenses/{id}`
- CRUD для расходов (`Expense`):
  - создание — `POST /api/expenses` (тело JSON, см. ниже);
  - обновление — `PUT /api/expenses/{id}` (полная замена полей);
  - удаление — `DELETE /api/expenses/{id}` (ответ `204 No Content`).
- Архитектура приложения разделена на слои `Controller -> Service -> Repository`.
- Используются DTO и маппер (`ExpenseResponseDto` и `ExpenseMapper`) для преобразования сущности в формат ответа API.
- В доменной модели не менее пяти сущностей; есть связи **OneToMany** (`User` → `Wallet`, `Wallet` → `Expense`, `Category` → `Expense`) и **ManyToMany** (`Expense` ↔ `Tag` через таблицу `expense_tags`).
- Демо транзакций: `POST /api/demo/related-save/partial` и `POST /api/demo/related-save/transactional` (тело `username`, `walletName`, `failAfterWallet`) — без `@Transactional` на сервисе возможен частичный коммит; с `@Transactional` при ошибке выполняется полный откат. Для каждого вызова используйте **новый уникальный** `username` (ограничение уникальности в БД).

## Технологии

- Java 21
- Spring Boot
- Spring Web (REST)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven

## Модель данных

Основные сущности:

- **`User`** — пользователь; имеет несколько кошельков (**OneToMany** → `Wallet`).
- **`Wallet`** — кошелёк/счёт; принадлежит пользователю (**ManyToOne** ← `User`), содержит много расходов (**OneToMany** → `Expense`).
- **`Category`** — категория расхода; к одной категории относится много расходов (**OneToMany** → `Expense`).
- **`Expense`** — расход; привязан к кошельку и категории (**ManyToOne**); может иметь несколько тегов (**ManyToMany** ↔ `Tag`).
- **`Tag`** — тег для классификации; связан с расходами через таблицу **`expense_tags`**.

В ответе API поле `category` — это **имя** сущности `Category`. Дополнительно возвращаются `walletId`, `userId` (владелец кошелька) и список имён тегов `tags`.

### FetchType и CascadeType

| Связь | FetchType | CascadeType | Зачем |
|-------|-----------|-------------|--------|
| `User` → `Wallet` | `LAZY` на коллекции | `PERSIST`, `MERGE`, `REMOVE` + `orphanRemoval` | Кошельки — часть агрегата пользователя; не тянем все кошельки без запроса; удаление пользователя удаляет кошельки. |
| `Wallet` → `User` | `LAZY` | нет | Владелец подгружается по необходимости. |
| `Wallet` → `Expense` | `LAZY` на коллекции | `PERSIST`, `MERGE`, `REMOVE` + `orphanRemoval` | Расходы принадлежат кошельку; удаление кошелька удаляет расходы. |
| `Expense` → `Wallet`, `Category` | `LAZY` | нет | Связи задаются в сервисе; справочник `Category` не каскадится. |
| `Expense` ↔ `Tag` | `LAZY` | нет | Теги общие; в коде подставляются по id. |
| `Category` → `Expense`, `Tag` → `Expense` | `LAZY` | нет | Обратные коллекции без каскада. |

Чтобы при выборке списка расходов не возникала проблема **N+1**, в `ExpenseRepository` для `findAll(Specification)` и `findByIdWithAssociations` задан **`@EntityGraph`** (`category`, `tags`, `wallet`, `wallet.user`). Фильтры для `GET /api/expenses` строятся в [`ExpenseSpecifications`](src/main/java/app/budgetmanager/repository/ExpenseSpecifications.java) и выполняются **в БД**, а не перебором всей таблицы в памяти. Чтение в сервисе помечено `@Transactional(readOnly = true)`.

## API

Фильтрация в `GET /api/expenses` работает через необязательные query-параметры (условия комбинируются через `AND` в одном запросе). Для даты используется формат ISO (`YYYY-MM-DD`).

Скрипт `data.sql` при старте очищает и заново наполняет таблицы (`TRUNCATE`) — удобно для разработки; для постоянных данных лучше отключить авто-инициализацию или использовать миграции.

Тело запроса для `POST` и `PUT` (JSON):

```json
{
  "description": "Coffee",
  "amount": 150.00,
  "date": "2025-03-01",
  "walletId": 1,
  "categoryId": 1,
  "tagIds": [1, 2]
}
```

Поле `tagIds` можно опустить или передать пустой массив — тегов не будет. Идентификаторы `walletId`, `categoryId`, `tagIds` должны существовать в БД.

## Качество кода

- Checkstyle настроен через Maven plugin и конфигурацию `checkstyle.xml`
- SonarCloud: https://sonarcloud.io/project/overview?id=IvanShevts0v_Budget-Manager

## Настройка и окружение

Пример текущих настроек в `application.yml`:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/budgetdb`
- `spring.datasource.username=postgres`
- `spring.datasource.password=${dbpassword}`
- `spring.jpa.hibernate.ddl-auto=update`
- `server.port=8080`

Перед запуском убедитесь, что PostgreSQL запущен и создана база `budgetdb`.

## Сборка и запуск

Запуск приложения:

```bash
mvn spring-boot:run
```

Сборка проекта и проверка стиля кода (Checkstyle):

```bash
mvn clean install
mvn checkstyle:check
```

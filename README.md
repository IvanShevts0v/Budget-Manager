# Менеджер бюджета (Budget Manager) — REST API

Проект представляет собой Spring Boot REST API для учёта расходов. Приложение хранит записи о расходах в PostgreSQL и позволяет получать их через HTTP-эндпоинты.

## Возможности

- **Пользователи:** `GET /users` (`Pageable`: `page`, `size`, `sort`), `GET /users/{id}`, `POST /users/register` (создаёт пользователя и кошелёк по умолчанию), `PATCH /users/{id}`, `DELETE /users/{id}`.
- **Кошельки:** `GET /wallets`, `GET /wallets?userId=` (оба — `Page`), `GET /wallets/{id}`, `POST /wallets`, `PATCH /wallets/{id}`, `DELETE /wallets/{id}`.
- **Категории и теги:** CRUD по путям `/categories` и `/tags`; списки — `Page`. Создание категории: `POST /categories` с телом `{"name":"..."}` (`CategoryRequestDto`).
- **Расходы:** `GET /expenses` (фильтры + пагинация), `GET /expenses/by-wallet-and-category` (JPQL/native + кэш), `GET /expenses/created-count`, `GET /expenses/{id}`, `POST /expenses`, `POST /expenses/bulk`, `POST /expenses/bulk/no-transactional`, `POST /expenses/no-transactional`, `PUT /expenses/{id}`, `DELETE /expenses/{id}`.
- **Асинхронные задачи** (как [student-forum](https://github.com/Yakush-A/student-forum) `/tasks`): `POST /tasks?userId=` (`@Async`), `POST /tasks/completable-future?userId=` (`CompletableFuture`), `GET /tasks/{id}?userId=` — формирование отчёта ~10 с, статусы `PENDING` → `IN_PROGRESS` → `DONE`.
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

Для списков (`GET /users`, `/wallets`, `/categories`, `/categories/by-name`, `/tags`, `/expenses`, `/expenses/by-wallet-and-category`) ответ — Spring `Page`: `content`, `totalPages`, `totalElements`. Параметры: `page`, `size` (по умолчанию 20, максимум 100), `sort`. Get-by-id не пагинируется.

`amount` должно быть **строго больше нуля**. `tagIds` можно опустить или передать `[]`.

### Bulk-операция и транзакции

Пакетное создание расходов (как `POST /tags/bulk` в [student-forum](https://github.com/Yakush-A/student-forum)):

```http
POST /expenses/bulk
Content-Type: application/json

[
  { "description": "Coffee", "amount": 5.00, "date": "2026-05-27", "walletId": 1, "categoryId": 1 },
  { "description": "Lunch", "amount": 12.50, "date": "2026-05-27", "walletId": 1, "categoryId": 1 }
]
```

| Эндпоинт | `@Transactional` на bulk | Поведение при ошибке на 2-м элементе |
|----------|--------------------------|--------------------------------------|
| `POST /expenses/bulk` | да | **Ничего** не остаётся в БД (откат всей пачки) |
| `POST /expenses/bulk/no-transactional` | нет | **Первый** расход уже в БД, второй не создаётся |

Проверка: отправьте массив из двух объектов, у второго укажите несуществующий `walletId`, затем `GET /expenses` — с `/bulk` список пустой, с `/bulk/no-transactional` виден первый расход.

Тело — JSON-массив. Элементы валидируются через `@Valid` и группу `FullValidation` (пустой массив → 400).

### JPQL / native query, пагинация и in-memory индекс

`GET /expenses/by-wallet-and-category` фильтрует по вложенным сущностям (`wallet.user.id`, `category.name`):

- JPQL: `?native=false` (`findAllWithFiltersJpql`)
- native SQL: `?native=true` (`findAllWithFiltersNative`)
- `Pageable`: `page`, `size`, `sort`

Повторные запросы с теми же параметрами отдаются из `HashMap` (`ExpenseFilterCache`, ключ `ExpenseQueryKey` с `equals`/`hashCode`). Кэш сбрасывается **после commit** транзакции при изменении расходов, категорий, кошельков, тегов и пользователей.

Unit-тесты сервиса: `mvn test` (`ExpenseServiceTest`, `AsyncTaskServiceTest`, `RaceConditionTest`, Mockito).

### Многопоточность и нагрузка

| Требование | Реализация |
|------------|------------|
| `@Async` / `CompletableFuture` | `AsyncTaskExecutor`, `POST /tasks`, `POST /tasks/completable-future` |
| Потокобезопасный счётчик | `AtomicCounter` (`AtomicInteger`) — инкремент при каждом `POST /expenses` (и bulk); `GET /expenses/created-count` |
| Race condition 50+ потоков | `RaceConditionTest` — 100 потоков, `NonAtomicCounter` &lt; ожидания, `AtomicCounter` = 50 000 |
| JMeter | [`jmeter/Plan.jmx`](jmeter/Plan.jmx), инструкция [`jmeter/README.md`](jmeter/README.md) |

**Данные в PostgreSQL** между перезапусками сохраняются; при пустой БД сначала создайте пользователя (`/users/register`), категорию (`POST /categories`) и при необходимости теги.

## Frontend (React SPA)

Клиент в папке [`frontend/`](frontend/) — React + TypeScript + Vite, по образцу [student-forum](https://github.com/Yakush-A/student-forum).

| Требование | Реализация |
|------------|------------|
| SPA | React 18, react-router-dom |
| Работа с API | `fetch` через `/api` proxy → backend :8080 |
| OneToMany | User → Wallets (`UserPage`, `WalletsPage`), Wallet → Expenses, Category → Expenses |
| ManyToMany | Expense ↔ Tag — чекбоксы в модалке, chips в списке |
| CRUD + фильтрация | все ресурсы; expenses: query filters + paginated endpoint |

```bash
cd frontend
npm install
npm run dev
```

Открыть **http://localhost:5173** (backend на :8080). Подробнее: [`frontend/README.md`](frontend/README.md).

## Качество кода

- Checkstyle (`checkstyle.xml`), проверяется только `src/main/java` (без сгенерированных MapStruct-реализаций).
- SonarCloud: https://sonarcloud.io/project/overview?id=IvanShevts0v_Budget-Manager
  - Coverage приезжает из CI (JaCoCo → `sonar:sonar`). Нужен секрет `SONAR_TOKEN` и в Sonar: **Administration → Analysis Method → CI** (выключить Automatic Analysis).
  - Пакет сервисов: **Measures → Coverage → `app.budgetmanager.service`**.

## Настройка и окружение

Переменные окружения (см. [`.env.example`](.env.example)):

| Переменная | Описание | По умолчанию (local) |
|------------|----------|----------------------|
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://localhost:5432/budgetdb` |
| `SPRING_DATASOURCE_USERNAME` | пользователь БД | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | пароль БД | — |
| `FRONTEND_PROD_HOST_URL` | CORS для фронта | `http://localhost:5173` |
| `SERVER_PORT` | порт backend | `8080` |

Healthcheck: `GET /actuator/health` (Spring Actuator).

## Docker и Docker Compose

По образцу [student-forum](https://github.com/Yakush-A/student-forum):

```bash
cp .env.example .env
# отредактируй POSTGRES_PASSWORD при необходимости

docker compose up --build
```

| Сервис | URL |
|--------|-----|
| Frontend (nginx) | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |

Compose поднимает **PostgreSQL 16**, **backend** (Dockerfile в корне), **frontend** (nginx + proxy `/api` → backend).  
Healthcheck: `pg_isready`, `curl /actuator/health`, `curl /` на frontend.

## CI/CD (GitHub Actions)

Workflows в [`.github/workflows/`](.github/workflows/):

| Workflow | PR | push `main` |
|----------|-----|-------------|
| `backend.yml` | `mvn test`, `mvn package` | + deploy на Railway + healthcheck `/actuator/health` |
| `frontend.yml` | `npm ci`, `npm run build` | + deploy на Railway + healthcheck `/` |

После деплоя workflow опрашивает задеплоенное приложение (до 30 попыток с интервалом 10 с) и падает, если backend не отдаёт `"status":"UP"`, а frontend — HTTP 200. Если URL-секрет не задан, шаг healthcheck пропускается.

### Secrets для Railway (Settings → Secrets)

| Secret | Описание |
|--------|----------|
| `RAILWAY_TOKEN` | токен из [Railway](https://railway.app) → Account Settings |
| `RAILWAY_BACKEND_SERVICE` | имя/ID сервиса backend |
| `RAILWAY_FRONTEND_SERVICE` | имя/ID сервиса frontend |
| `BACKEND_PUBLIC_URL` | публичный URL backend, например `https://your-backend.up.railway.app` |
| `FRONTEND_PUBLIC_URL` | публичный URL frontend, например `https://your-frontend.up.railway.app` |

### Деплой на Railway (PaaS)

1. Создай проект на [Railway](https://railway.app).
2. Добавь **PostgreSQL** — скопируй переменные подключения.
3. **Backend service:** root directory = `/`, Dockerfile = `Dockerfile`.  
   Variables:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:PORT/DB`
   - `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `FRONTEND_PROD_HOST_URL=https://your-frontend.up.railway.app`
4. **Frontend service:** root directory = `frontend`, Dockerfile = `frontend/Dockerfile`.  
   Variables:
   - `BACKEND_HOST` = internal hostname backend в Railway
   - `BACKEND_PORT` = `8080`
5. Подключи GitHub repo → push в `main` запускает CI/CD.

## Сборка и запуск (локально)

```bash
$env:DB_PASSWORD="postgres"
mvn spring-boot:run
```

```bash
mvn clean install
mvn test
mvn checkstyle:check
```

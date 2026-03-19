# Менеджер бюджета (Budget Manager) — REST API

Проект представляет собой Spring Boot REST API для учёта расходов. Приложение хранит записи о расходах в базе (по умолчанию H2 в памяти) и позволяет получать их через HTTP-эндпоинты.

## Возможности

- Просмотр списка расходов с фильтрацией по любым полям сущности `Expense`:
  - `GET /api/expenses?id=&description=&amount=&category=&date=YYYY-MM-DD`
- Получение расхода по идентификатору:
  - `GET /api/expenses/{id}`
- Архитектура приложения разделена на слои `Controller -> Service -> Repository`.
- Используются DTO и маппер (`ExpenseResponseDto` и `ExpenseMapper`) для преобразования сущности в формат ответа API.

## Технологии

- Java 21
- Spring Boot
- Spring Web (REST)
- Spring Data JPA (Hibernate)
- H2 Database (in-memory)
- Maven

## Модель данных

В проекте используется основная сущность:

- **`Expense`** — расход пользователя.

Таблица: `expenses`

Поля сущности:

- `id` — первичный ключ (генерируется автоматически)
- `description` — описание расхода (обязательное)
- `amount` — сумма расхода (обязательное, `precision = 19`, `scale = 2`)
- `category` — категория расхода (обязательное)
- `date` — дата расхода (обязательное)

## API

Фильтрация в `GET /api/expenses` работает через необязательные query-параметры. Для даты используется формат ISO (`YYYY-MM-DD`).

## Качество кода

- Checkstyle настроен через Maven plugin и конфигурацию `checkstyle.xml`
- SonarCloud: https://sonarcloud.io/project/overview?id=IvanShevts0v_Budget-Manager

## Настройка и окружение

В `application.yml` включена H2 консоль, а также задано создание схемы на старте приложения:

- `spring.datasource.url=jdbc:h2:mem:budgetdb`
- `spring.jpa.hibernate.ddl-auto=create-drop`
- `server.port=8080`

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

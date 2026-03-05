# Менеджер бюджета — Spring Boot REST API

1. Создано Spring Boot приложение.
2. Реализован REST API для сущности Expense (тема «Менеджер бюджета»).
3. Реализованы:
   - GET-эндпоинт с @RequestParam для поиска по любому полю Expense;
   - GET-эндпоинт с @PathVariable для получения по id.
4. Реализованы слои: Controller → Service → Repository.
5. Реализованы DTO и маппер между сущностью и ответом API.
6. Настроен Checkstyle, код приведён к Google style.

## Запуск

Команда для запуска приложения:

```bash
mvn spring-boot:run
```

## Сборка и проверка кода

Сборка проекта и проверка стиля кода (Checkstyle):

```bash
mvn clean install
mvn checkstyle:check
```

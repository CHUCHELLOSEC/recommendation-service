# Recommendation Service

Сервис персональных рекомендаций банковских продуктов для банка «Стар».

Сервис анализирует транзакции клиента и предлагает подходящие продукты:

* по **статическим правилам**, зашитым в код: «Invest 500», «Top Saving», «Простой кредит»;
* по **динамическим правилам**, которые менеджер создаёт через REST API без релиза сервиса.

Рекомендации доступны по REST API (`GET /recommendation/{user_id}`) и в **Telegram-боте** (`/recommend username`).
Менеджеры видят статистику срабатываний правил, внешние системы могут сбрасывать кеши и узнавать версию сервиса.

**Стек:** Java 17 · Spring Boot 3.2 · H2 (транзакции, только чтение) · PostgreSQL + Liquibase (правила) ·
Caffeine · Telegram Bot API · springdoc-openapi · JUnit 5 / Mockito.

## Быстрый старт

```bash
./mvnw clean package
java -jar target/recommendation-service-0.0.1-SNAPSHOT.jar
```

Перед запуском нужны PostgreSQL и файл `transaction.mv.db`; подробности и переменные окружения
описаны в [инструкции по развертыванию](docs/wiki/Deployment.md).

После запуска: Swagger UI на <http://localhost:8080/swagger-ui.html>.

## Документация

| Документ                                                     | Содержание                                                   |
|--------------------------------------------------------------|--------------------------------------------------------------|
| [Главная вики](docs/wiki/Home.md)                            | Краткое описание, стек, исполнители                          |
| [Требования](docs/wiki/Requirements.md)                      | User Stories, нефункциональные требования, диаграмма прецедентов |
| [Статус реализации требований](docs/wiki/Traceability.md)    | Связь требований с Issues, ветками и PR                      |
| [Архитектура](docs/wiki/Architecture.md)                     | Диаграмма компонентов, диаграмма деятельности, модель данных |
| [REST API](docs/wiki/REST-API.md) · [openapi.yaml](docs/openapi.yaml) | Описание эндпоинтов в формате OpenAPI (Swagger)      |
| [Развертывание](docs/wiki/Deployment.md)                     | Сервисы, сборка, запуск, переменные окружения                |

Те же страницы опубликованы в [GitHub Wiki](https://github.com/CHUCHELLOSEC/recommendation-service/wiki).

## Структура проекта

```
src/main/java/pro/sky/recommendation
├── bot/             Telegram-бот
├── configuration/   два DataSource (H2 и PostgreSQL), клиент Telegram
├── controller/      REST-контроллеры и обработчик ошибок
├── dto/             объекты ответов API
├── model/           JPA-сущности динамических правил и статистики
├── repository/      SQL к H2 с кешами, JPA-репозитории PostgreSQL
├── rules/           статические правила (RecommendationRuleSet)
└── service/         бизнес-логика: подбор рекомендаций, динамические правила, статистика
```

## Тесты

```bash
./mvnw test
```

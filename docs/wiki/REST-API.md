# REST API

Полная спецификация в формате **OpenAPI 3**: [`docs/openapi.yaml`](https://github.com/CHUCHELLOSEC/recommendation-service/blob/master/docs/openapi.yaml).
Её можно открыть в [Swagger Editor](https://editor.swagger.io/) (File → Import URL / вставить содержимое).

У запущенного сервиса спецификация генерируется автоматически (springdoc-openapi):

| Что                | URL                                         |
|--------------------|---------------------------------------------|
| Swagger UI         | `http://localhost:8080/swagger-ui.html`     |
| OpenAPI JSON       | `http://localhost:8080/v3/api-docs`         |
| OpenAPI YAML       | `http://localhost:8080/v3/api-docs.yaml`    |

## Сводка эндпоинтов

| Метод    | Путь                            | Актор            | Требование     | Описание                                         | Успешный ответ |
|----------|---------------------------------|------------------|----------------|--------------------------------------------------|----------------|
| `GET`    | `/recommendation/{user_id}`     | Пользователь     | US-01…US-05    | Рекомендации для клиента                         | `200`          |
| `POST`   | `/rule`                         | Менеджер         | US-09, US-10   | Создать динамическое правило                     | `200`          |
| `GET`    | `/rule`                         | Менеджер         | US-11          | Все динамические правила                         | `200`          |
| `DELETE` | `/rule/{product_id}`            | Менеджер         | US-12          | Удалить правило продукта                         | `204`          |
| `GET`    | `/rule/stats`                   | Менеджер         | US-13          | Статистика срабатываний                          | `200`          |
| `POST`   | `/management/clear-caches`      | Внешняя система  | US-14          | Сбросить кеши                                    | `200`          |
| `GET`    | `/management/info`              | Внешняя система  | US-15          | Имя и версия сервиса                             | `200`          |

Ошибки валидации возвращаются как `400 Bad Request` с телом `{"error": "<описание>"}`.

## Рекомендации

### `GET /recommendation/{user_id}`

```http
GET /recommendation/cd515076-5d8a-44be-930e-8d4fcb79f42d
```

```json
{
  "user_id": "cd515076-5d8a-44be-930e-8d4fcb79f42d",
  "recommendations": [
    {
      "name": "Invest 500",
      "id": "147f6a0f-3b91-413b-ab99-87f081d60d5a",
      "text": "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС)..."
    }
  ]
}
```

## Динамические правила

### `POST /rule`

Тело запроса:

| Поле           | Тип              | Обязательное | Описание                                    |
|----------------|------------------|--------------|---------------------------------------------|
| `product_name` | string           | да           | Название продукта                           |
| `product_id`   | UUID             | да           | Идентификатор продукта                      |
| `product_text` | string           | да           | Текст рекомендации                          |
| `rule`         | array<RuleQuery> | да, ≥ 1      | Запросы, объединённые логическим «И»        |

`RuleQuery`:

| Запрос (`query`)                              | `arguments`                                              | Истинно, когда                                              |
|-----------------------------------------------|----------------------------------------------------------|-------------------------------------------------------------|
| `USER_OF`                                     | `[productType]`                                          | есть хотя бы одна транзакция по продукту типа               |
| `ACTIVE_USER_OF`                              | `[productType]`                                          | не меньше 5 транзакций по продукту типа                     |
| `TRANSACTION_SUM_COMPARE`                     | `[productType, transactionType, operator, constant]`     | `Σ транзакций типа <operator> constant`                     |
| `TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW`    | `[productType, operator]`                                | `Σ DEPOSIT <operator> Σ WITHDRAW`                           |

* `productType`: `DEBIT`, `CREDIT`, `INVEST`, `SAVING`
* `transactionType`: `DEPOSIT`, `WITHDRAW`
* `operator`: `>`, `<`, `=`, `>=`, `<=`
* `constant`: неотрицательное целое число
* `negate: true` инвертирует результат запроса

Пример: статическое правило «Простой кредит», записанное как динамическое.

```json
{
  "product_name": "Простой кредит",
  "product_id": "ab138afb-f3ba-4a93-b74f-0fcee86d447f",
  "product_text": "<текст рекомендации>",
  "rule": [
    { "query": "USER_OF", "arguments": ["CREDIT"], "negate": true },
    { "query": "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW", "arguments": ["DEBIT", ">"], "negate": false },
    { "query": "TRANSACTION_SUM_COMPARE", "arguments": ["DEBIT", "WITHDRAW", ">", "100000"], "negate": false }
  ]
}
```

Ответ `200`: то же правило с полем `"id"`, выданным сервером. Ответ `400`:

```json
{ "error": "Запрос USER_OF принимает аргументов: 1" }
```

### `GET /rule`

```json
{ "data": [ { "id": "…", "product_name": "…", "product_id": "…", "product_text": "…", "rule": [ … ] } ] }
```

### `DELETE /rule/{product_id}`

Удаляет правила продукта вместе со статистикой. Ответ: `204 No Content`.

### `GET /rule/stats`

```json
{
  "stats": [
    { "rule_id": "2f8a1c3e-8b1d-4c6a-9e4b-5a0f3c2d1e7b", "count": 12 },
    { "rule_id": "7d2e9b4a-1c3f-4e5d-8a6b-9c0d1e2f3a4b", "count": 0 }
  ]
}
```

## Служебные эндпоинты

### `POST /management/clear-caches`

Без тела. Очищает все кеши запросов к основной БД. Ответ: `200 OK` с пустым телом.

### `GET /management/info`

```json
{ "name": "recommendation-service", "version": "0.0.1-SNAPSHOT" }
```

Значения берутся из `META-INF/build-info.properties`, который создаётся при сборке Maven.
При запуске из IDE без сборки возвращается `"version": "unknown"`.

## Telegram-бот

Не REST, но тоже внешний интерфейс пользователя:

| Сообщение                    | Ответ                                                                                    |
|------------------------------|------------------------------------------------------------------------------------------|
| `/recommend username`        | «Здравствуйте Имя Фамилия», «Новые продукты для вас:» и список названий продуктов        |
| `/recommend` без аргумента   | «Укажите имя пользователя: /recommend username»                                          |
| неизвестный `username`       | «Пользователь не найден»                                                                 |
| `/start` или любой текст     | Приветствие и справка                                                                    |

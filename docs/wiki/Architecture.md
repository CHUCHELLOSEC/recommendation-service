# Архитектура приложения

Монолитное Spring Boot приложение с классическим разделением на слои:
**контроллеры / бот → сервисы → правила → репозитории → базы данных**.

## Диаграмма компонентов

Стрелки показывают направление вызовов. Слева — источники внешних вызовов, справа — внешние системы,
к которым обращается приложение.

```mermaid
flowchart LR
    %% ===== Внешние инициаторы вызовов =====
    manager(["👤 Менеджер<br/>(HTTP-клиент)"])
    client(["👤 Приложение банка<br/>(HTTP-клиент)"])
    external(["⚙️ Внешняя система"])
    tgUser(["👤 Пользователь Telegram"])

    tgApi[["Telegram Bot API<br/>api.telegram.org"]]

    subgraph app["Recommendation Service (Spring Boot)"]
        direction LR

        subgraph web["Входные точки"]
            recCtrl["RecommendationController<br/>GET /recommendation/{user_id}"]
            ruleCtrl["RuleController<br/>POST · GET /rule<br/>DELETE /rule/{product_id}<br/>GET /rule/stats"]
            mgmtCtrl["ManagementController<br/>POST /management/clear-caches<br/>GET /management/info"]
            bot["RecommendationBot<br/>(UpdatesListener)"]
            advice["ApiExceptionHandler<br/>400 Bad Request"]
        end

        subgraph services["Сервисы"]
            recSvc["RecommendationService"]
            dynSvc["DynamicRecommendationService"]
            ruleSvc["DynamicRuleService"]
            statSvc["RuleStatService"]
            userSvc["UserService"]
            evaluator["RuleQueryEvaluator"]
        end

        subgraph rules["Статические правила<br/>(RecommendationRuleSet)"]
            invest["Invest500RuleSet"]
            saving["TopSavingRuleSet"]
            credit["SimpleCreditRuleSet"]
        end

        subgraph repos["Репозитории"]
            recRepo["RecommendationsRepository<br/>JdbcTemplate + кеши Caffeine"]
            ruleRepo["DynamicRuleRepository<br/>(JPA)"]
            statRepo["RuleStatRepository<br/>(JPA, upsert)"]
        end

        buildInfo[("META-INF/<br/>build-info.properties")]
    end

    h2[("H2 transaction.mv.db<br/>users · products · transactions<br/>только чтение")]
    pg[("PostgreSQL<br/>dynamic_rule · rule_stat<br/>миграции Liquibase")]

    %% ===== Внешние вызовы в приложение =====
    client -- "HTTP" --> recCtrl
    manager -- "HTTP" --> ruleCtrl
    external -- "HTTP" --> mgmtCtrl
    tgUser -- "сообщения" --> tgApi
    tgApi -- "long polling getUpdates" --> bot
    bot -- "sendMessage" --> tgApi

    %% ===== Внутренние вызовы =====
    recCtrl --> recSvc
    bot --> userSvc
    bot --> recSvc
    ruleCtrl --> ruleSvc
    ruleCtrl --> statSvc
    mgmtCtrl -- "clearCaches()" --> recRepo
    mgmtCtrl -.-> buildInfo

    recSvc --> invest & saving & credit
    recSvc --> dynSvc
    dynSvc --> ruleRepo
    dynSvc --> evaluator
    dynSvc -- "increment()" --> statSvc
    ruleSvc --> ruleRepo
    ruleSvc -- "validate()" --> evaluator
    statSvc --> statRepo
    statSvc --> ruleRepo
    userSvc --> recRepo
    evaluator --> recRepo
    invest & saving & credit --> recRepo

    %% ===== Вызовы внешних систем хранения =====
    recRepo -- "SQL (JDBC)" --> h2
    ruleRepo -- "JPA / SQL" --> pg
    statRepo -- "JPA / SQL" --> pg
```

### Компоненты

| Компонент                          | Назначение                                                                                                                                         |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `RecommendationController`         | REST-вход для получения рекомендаций.                                                                                                              |
| `RuleController`                   | REST API менеджера: CRUD динамических правил и статистика.                                                                                          |
| `ManagementController`             | Служебный API для внешних систем: сброс кешей, имя и версия сборки (из `build-info.properties`, генерируется `spring-boot-maven-plugin`).          |
| `RecommendationBot`                | Telegram-бот: получает сообщения long polling'ом через `java-telegram-bot-api`, отвечает рекомендациями.                                            |
| `ApiExceptionHandler`              | Превращает `IllegalArgumentException` в `400 {"error": "..."}`.                                                                                    |
| `RecommendationService`            | Точка сборки рекомендаций: вызывает все статические правила, затем динамические, убирает дубликаты по `id` продукта.                             |
| `RecommendationRuleSet` и реализации | Статические правила из ТЗ 1, зашитые в код. Spring внедряет все реализации списком, поэтому новое правило = новый `@Component`.                   |
| `DynamicRecommendationService`     | Перебирает динамические правила, вычисляет их и увеличивает счётчик сработавших.                                                                    |
| `RuleQueryEvaluator`               | Валидирует и вычисляет запросы `USER_OF`, `ACTIVE_USER_OF`, `TRANSACTION_SUM_COMPARE`, `TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW`.               |
| `DynamicRuleService`               | Создание (с валидацией), чтение и удаление динамических правил.                                                                                   |
| `RuleStatService`                  | Атомарное увеличение счётчика и выдача статистики по всем правилам.                                                                                 |
| `UserService`                      | Поиск клиента по `username` для бота (только единственное совпадение).                                                                              |
| `RecommendationsRepository`        | Единственное место с SQL к H2. Три кеша Caffeine: число транзакций, суммы, пользователи по `username`.                                             |
| `DataSourceConfiguration`          | Два `DataSource`: H2 (`@Primary`, для `JdbcTemplate`) и PostgreSQL (для JPA и Liquibase), отдельный `rulesTransactionManager`.                    |

## Диаграмма деятельности: выдача рекомендаций

Алгоритм `RecommendationService.getRecommendations(userId)`, общий для REST API и Telegram-бота.

```mermaid
flowchart TD
    start([Начало: запрос рекомендаций для user_id])
    start --> src{Источник запроса?}
    src -- "REST: GET /recommendation/{user_id}" --> init
    src -- "Telegram: /recommend username" --> findUser[Найти клиентов по username<br/>кеш usersCache → H2]
    findUser --> one{Найден ровно один?}
    one -- "нет (0 или >1)" --> notFound[/Ответ: «Пользователь не найден»/] --> finish([Конец])
    one -- да --> init

    init[Создать пустой упорядоченный набор<br/>рекомендаций по id продукта]

    %% ===== Статические правила =====
    init --> s1

    subgraph static["Статические правила (ТЗ 1)"]
        s1{"Invest 500:<br/>USER_OF DEBIT<br/>и НЕ USER_OF INVEST<br/>и Σ SAVING DEPOSIT > 1000?"}
        s1 -- да --> a1[Добавить «Invest 500»]
        s1 -- нет --> s2
        a1 --> s2

        s2{"Top Saving:<br/>USER_OF DEBIT?"}
        s2 -- нет --> s3
        s2 -- да --> s2b{"Σ DEBIT DEPOSIT ≥ 50 000<br/>или Σ SAVING DEPOSIT ≥ 50 000?"}
        s2b -- нет --> s3
        s2b -- да --> s2c{"Σ DEBIT DEPOSIT ><br/>Σ DEBIT WITHDRAW?"}
        s2c -- нет --> s3
        s2c -- да --> a2[Добавить «Top Saving»] --> s3

        s3{"Простой кредит:<br/>НЕ USER_OF CREDIT?"}
        s3 -- нет --> sEnd
        s3 -- да --> s3b{"Σ DEBIT DEPOSIT > Σ DEBIT WITHDRAW<br/>и Σ DEBIT WITHDRAW > 100 000?"}
        s3b -- да --> a3[Добавить «Простой кредит»] --> sEnd
        s3b -- нет --> sEnd
        sEnd((" "))
    end

    %% ===== Динамические правила =====
    sEnd --> loadRules[Загрузить все динамические правила<br/>из PostgreSQL]

    subgraph dynamic["Динамические правила (ТЗ 2–3)"]
        loadRules --> hasNext{Есть ещё правило?}
        hasNext -- да --> nextQuery{Есть ещё запрос<br/>в правиле?}
        nextQuery -- да --> eval[Вычислить запрос:<br/>USER_OF / ACTIVE_USER_OF /<br/>TRANSACTION_SUM_COMPARE /<br/>TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW<br/>данные из кеша или H2]
        eval --> neg{negate = true?}
        neg -- да --> inv[Инвертировать результат] --> qres
        neg -- нет --> qres{Результат true?}
        qres -- да --> nextQuery
        qres -- "нет: правило не выполнено" --> hasNext
        nextQuery -- "нет: все запросы true" --> inc[rule_stat: счётчик +1<br/>атомарный upsert]
        inc --> dup{Продукт уже<br/>в наборе?}
        dup -- да --> hasNext
        dup -- нет --> addDyn[Добавить продукт правила] --> hasNext
    end

    hasNext -- нет --> resp{Источник запроса?}
    resp -- REST --> json[/JSON: user_id и recommendations/] --> finish
    resp -- Telegram --> msg[/«Здравствуйте Имя Фамилия»<br/>«Новые продукты для вас:» + список/] --> finish
```

### Ключевые решения

* **Дедупликация.** Рекомендации собираются в `LinkedHashMap` по `id` продукта: сначала статические, затем динамические.
  Если продукт есть и там и там, в ответе остаётся первая (статическая) рекомендация.
* **Счётчик статистики** увеличивается для каждого выполненного динамического правила, даже если его продукт уже был
  в ответе: статистика отражает срабатывание правила, а не показ продукта.
* **Кеширование.** Все обращения к H2 идут через `RecommendationsRepository`, который кеширует результаты по ключу
  `(userId, productType[, transactionType])`. Внешняя система сбрасывает кеш через `POST /management/clear-caches`
  после обновления данных в H2.
* **Ленивое вычисление.** Статические правила сначала проверяют самое дешёвое условие; динамическое правило перестаёт
  вычисляться на первом ложном запросе (`allMatch`).

## Модель данных

```mermaid
erDiagram
    users ||--o{ transactions : "user_id"
    products ||--o{ transactions : "product_id"
    users {
        UUID id PK
        VARCHAR username
        VARCHAR first_name
        VARCHAR last_name
    }
    products {
        UUID id PK
        VARCHAR type "DEBIT | CREDIT | INVEST | SAVING"
    }
    transactions {
        UUID id PK
        UUID user_id FK
        UUID product_id FK
        VARCHAR type "DEPOSIT | WITHDRAW"
        INT amount
    }

    dynamic_rule ||--o| rule_stat : "rule_id, ON DELETE CASCADE"
    dynamic_rule {
        UUID id PK
        TEXT product_name
        UUID product_id
        TEXT product_text
        JSONB rule
    }
    rule_stat {
        UUID id PK
        UUID rule_id FK "UNIQUE"
        BIGINT trigger_count
    }
```

Таблицы `users`, `products`, `transactions` находятся в H2 (только чтение), `dynamic_rule` и `rule_stat` — в PostgreSQL.

## Как добавить новую функциональность

**Новое статическое правило.** Создать класс в пакете `rules`, реализующий `RecommendationRuleSet`, и пометить его
`@Component`. Данные брать только через `RecommendationsRepository`, чтобы работал кеш. `RecommendationService`
подхватит правило автоматически.

**Новый тип запроса для динамических правил** (например, `TRANSACTION_COUNT_COMPARE`):

1. Если нужен новый SQL, добавить метод с собственным кешем в `RecommendationsRepository` и очистку этого кеша в `clearCaches()`.
2. Добавить ветку в `RuleQueryEvaluator.validate()` с проверкой числа и значений аргументов.
3. Добавить ветку в `RuleQueryEvaluator.evaluate()`.
4. Добавить тесты в `RuleQueryEvaluatorTest` и описать запрос на страницах [Требования](Requirements) и [REST API](REST-API).

Схема БД при этом не меняется: запросы хранятся в JSONB-колонке `dynamic_rule.rule`.

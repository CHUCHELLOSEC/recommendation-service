# Статус реализации требований

Каждое требование из [страницы требований](Requirements) привязано к одной или нескольким Issues на GitHub.
Каждая Issue ссылается на ветку и Pull Request, в которых выполнено изменение.

Репозиторий: <https://github.com/CHUCHELLOSEC/recommendation-service>

## Ветки и Pull Request

| Этап                | Ветка                                                                                             | Pull Request                                                                      | Коммиты               |
|---------------------|---------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|-----------------------|
| ТЗ 1                | [`master`](https://github.com/CHUCHELLOSEC/recommendation-service/tree/master)                    | начальный коммит                                                                  | `3d16c3f`, `603e264`  |
| ТЗ 2                | [`develop`](https://github.com/CHUCHELLOSEC/recommendation-service/tree/develop)                  | [PR #1](https://github.com/CHUCHELLOSEC/recommendation-service/pull/1)            | `ed3138a`             |
| ТЗ 3                | [`develop1.1`](https://github.com/CHUCHELLOSEC/recommendation-service/tree/develop1.1)            | [PR #2](https://github.com/CHUCHELLOSEC/recommendation-service/pull/2)            | `6653a2d`             |
| Курсовая            | [`coursework-docs`](https://github.com/CHUCHELLOSEC/recommendation-service/tree/coursework-docs)  | [PR #14](https://github.com/CHUCHELLOSEC/recommendation-service/pull/14)          | —                     |

## Issues

| Issue                                                                     | Название                                                                    | Требования                         | Ветка / PR                  | Статус |
|---------------------------------------------------------------------------|-----------------------------------------------------------------------------|------------------------------------|-----------------------------|--------|
| [#3](https://github.com/CHUCHELLOSEC/recommendation-service/issues/3)     | [ТЗ1] Каркас Spring Boot и подключение к основной БД H2 (только чтение)    | NFR-02                             | `master`                    | Done   |
| [#4](https://github.com/CHUCHELLOSEC/recommendation-service/issues/4)     | [ТЗ1] REST-эндпоинт GET /recommendation/{user_id}                           | US-01                              | `master`                    | Done   |
| [#5](https://github.com/CHUCHELLOSEC/recommendation-service/issues/5)     | [ТЗ1] Статические правила Invest 500, Top Saving, Простой кредит           | US-02, US-03, US-04                | `master`                    | Done   |
| [#6](https://github.com/CHUCHELLOSEC/recommendation-service/issues/6)     | [ТЗ2] Вторая БД PostgreSQL и миграции Liquibase                             | NFR-03                             | `develop`, PR #1            | Done   |
| [#7](https://github.com/CHUCHELLOSEC/recommendation-service/issues/7)     | [ТЗ2] REST API динамических правил: создание, просмотр, удаление           | US-09, US-11, US-12, NFR-06        | `develop`, PR #1            | Done   |
| [#8](https://github.com/CHUCHELLOSEC/recommendation-service/issues/8)     | [ТЗ2] Вычисление запросов динамических правил                               | US-05, US-10                       | `develop`, PR #1            | Done   |
| [#9](https://github.com/CHUCHELLOSEC/recommendation-service/issues/9)     | [ТЗ2] Кеширование запросов к основной БД (Caffeine)                         | NFR-01                             | `develop`, PR #1            | Done   |
| [#10](https://github.com/CHUCHELLOSEC/recommendation-service/issues/10)   | [ТЗ3] Telegram-бот: команда /recommend, приветствие и справка              | US-06, US-07, US-08, NFR-04, NFR-05 | `develop1.1`, PR #2        | Done   |
| [#11](https://github.com/CHUCHELLOSEC/recommendation-service/issues/11)   | [ТЗ3] Статистика срабатываний динамических правил GET /rule/stats          | US-13, NFR-07                      | `develop1.1`, PR #2         | Done   |
| [#12](https://github.com/CHUCHELLOSEC/recommendation-service/issues/12)   | [ТЗ3] Management API: сброс кешей и информация о сервисе                   | US-14, US-15                       | `develop1.1`, PR #2         | Done   |
| [#13](https://github.com/CHUCHELLOSEC/recommendation-service/issues/13)   | [Курсовая] Рефакторинг, Javadoc, тесты, Swagger и проектная документация  | NFR-08, NFR-09, NFR-10             | `coursework-docs`, PR #14   | Done   |

## Матрица требований

| Требование | Описание (кратко)                                   | Issue        | Где в коде                                                                    | Статус |
|------------|-----------------------------------------------------|--------------|-------------------------------------------------------------------------------|--------|
| US-01      | Рекомендации по `user_id`                           | #4           | `RecommendationController`, `RecommendationService`                          | ✅     |
| US-02      | Правило «Invest 500»                                | #5           | `rules/Invest500RuleSet`                                                      | ✅     |
| US-03      | Правило «Top Saving»                                | #5           | `rules/TopSavingRuleSet`                                                      | ✅     |
| US-04      | Правило «Простой кредит»                            | #5           | `rules/SimpleCreditRuleSet`                                                   | ✅     |
| US-05      | Рекомендации по динамическим правилам               | #8           | `DynamicRecommendationService`, `RecommendationService`                      | ✅     |
| US-06      | Команда бота `/recommend username`                  | #10          | `bot/RecommendationBot`, `UserService`                                        | ✅     |
| US-07      | «Пользователь не найден»                            | #10          | `UserService.findUniqueByUsername`                                            | ✅     |
| US-08      | Приветствие и справка в боте                        | #10          | `bot/RecommendationBot`                                                       | ✅     |
| US-09      | Создание динамического правила                      | #7           | `RuleController.add`, `DynamicRuleService.add`                                | ✅     |
| US-10      | Запросы правил и `negate`                           | #8           | `RuleQueryEvaluator`                                                          | ✅     |
| US-11      | Просмотр правил                                     | #7           | `RuleController.getAll`                                                       | ✅     |
| US-12      | Удаление правила                                    | #7           | `RuleController.delete`, FK `ON DELETE CASCADE`                               | ✅     |
| US-13      | Статистика срабатываний                             | #11          | `RuleStatService`, `RuleStatRepository.increment`                             | ✅     |
| US-14      | Сброс кешей                                         | #12          | `ManagementController.clearCaches`                                            | ✅     |
| US-15      | Имя и версия сервиса                                | #12          | `ManagementController.info`, `build-info` в `pom.xml`                         | ✅     |
| NFR-01     | Кеширование                                         | #9           | `RecommendationsRepository` (Caffeine)                                        | ✅     |
| NFR-02     | Основная БД только для чтения                       | #3           | `application.properties`, `DataSourceConfiguration`                           | ✅     |
| NFR-03     | PostgreSQL и Liquibase                              | #6           | `DataSourceConfiguration`, `db/changelog/changelog-master.sql`                | ✅     |
| NFR-04     | Логирование ошибок                                  | #10          | `RecommendationBot.process`, `RecommendationBot.send`                         | ✅     |
| NFR-05     | Запуск без Telegram                                 | #10          | `RecommendationBot.init`, `TelegramBotConfiguration`                          | ✅     |
| NFR-06     | Валидация входных данных                            | #7           | `RuleQueryEvaluator.validate`, `ApiExceptionHandler`                          | ✅     |
| NFR-07     | Атомарный счётчик                                   | #11          | `RuleStatRepository.increment` (upsert)                                       | ✅     |
| NFR-08     | OpenAPI / Swagger, Javadoc                          | #13          | springdoc в `pom.xml`, [REST API](REST-API)                                   | ✅     |
| NFR-09     | Конфигурация через переменные окружения             | #13          | `application.properties`, [Развертывание](Deployment)                         | ✅     |
| NFR-10     | Форматирование и тесты                              | #13          | `src/test/java/...`                                                           | ✅     |

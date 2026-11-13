# Развертывание

## Необходимые сервисы

| Сервис                    | Версия          | Зачем                                                                                                         |
|---------------------------|-----------------|---------------------------------------------------------------------------------------------------------------|
| JDK / JRE                 | 17+             | Сборка (JDK) и запуск (JRE) приложения                                                                         |
| PostgreSQL                | 13+             | БД динамических правил и статистики. Нужна функция `gen_random_uuid()` (встроена с PostgreSQL 13)             |
| Файл БД H2 `transaction.mv.db` | —          | Основная БД с клиентами и транзакциями (только чтение). Встроенная, отдельный сервер не нужен                 |
| Telegram Bot API          | —               | Необязательно. Нужен токен бота от [@BotFather](https://t.me/BotFather) и исходящий доступ к `api.telegram.org` |

Maven устанавливать не нужно: в репозитории есть Maven Wrapper (`mvnw`, `mvnw.cmd`), он скачает Maven при первом запуске.

## 1. Подготовка PostgreSQL

Создайте пользователя и базу (таблицы создаст Liquibase при первом запуске):

```sql
CREATE USER rules_user WITH PASSWORD 'rules_pass';
CREATE DATABASE recommendation_rules OWNER rules_user;
```

## 2. Подготовка H2

Положите файл `transaction.mv.db` в рабочую директорию, из которой будет запускаться приложение
(по умолчанию URL `jdbc:h2:file:./transaction`), либо укажите путь к нему в `SPRING_DATASOURCE_URL`
без расширения `.mv.db`, например `jdbc:h2:file:/opt/recommendation/transaction`.

## 3. Сборка JAR

Linux / macOS:

```bash
./mvnw clean package
```

Windows:

```bat
mvnw.cmd clean package
```

Команда компилирует код, запускает модульные тесты и создаёт архив
`target/recommendation-service-0.0.1-SNAPSHOT.jar`. Собрать без тестов: `./mvnw clean package -DskipTests`.

## 4. Запуск

```bash
java -jar target/recommendation-service-0.0.1-SNAPSHOT.jar
```

С переменными окружения (Linux / macOS):

```bash
export SPRING_DATASOURCE_URL=jdbc:h2:file:/opt/recommendation/transaction
export RULES_DATASOURCE_JDBCURL=jdbc:postgresql://db.example.local:5432/recommendation_rules
export RULES_DATASOURCE_USERNAME=rules_user
export RULES_DATASOURCE_PASSWORD=secret
export TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
java -jar target/recommendation-service-0.0.1-SNAPSHOT.jar
```

Windows (PowerShell):

```powershell
$env:RULES_DATASOURCE_JDBCURL = "jdbc:postgresql://localhost:5432/recommendation_rules"
$env:RULES_DATASOURCE_USERNAME = "rules_user"
$env:RULES_DATASOURCE_PASSWORD = "secret"
$env:TELEGRAM_BOT_TOKEN = "123456:ABC-DEF..."
java -jar target\recommendation-service-0.0.1-SNAPSHOT.jar
```

Сервис стартует на порту `8080`. Проверка:

```bash
curl http://localhost:8080/management/info
```

## Переменные окружения

Spring Boot связывает переменные окружения со свойствами `application.properties` по правилу relaxed binding:
точки заменяются на `_`, дефисы удаляются, всё в верхнем регистре
(`rules.datasource.jdbc-url` → `RULES_DATASOURCE_JDBCURL`). Переменные окружения имеют приоритет над `application.properties`.

### Связь с базами данных (обязательно для рабочей среды)

| Переменная                    | Свойство                          | По умолчанию                                               | Описание                       |
|-------------------------------|-----------------------------------|------------------------------------------------------------|--------------------------------|
| `SPRING_DATASOURCE_URL`       | `spring.datasource.url`           | `jdbc:h2:file:./transaction`                               | JDBC URL основной БД H2        |
| `SPRING_DATASOURCE_USERNAME`  | `spring.datasource.username`      | `sa`                                                       | Пользователь H2                |
| `SPRING_DATASOURCE_PASSWORD`  | `spring.datasource.password`      | пусто                                                      | Пароль H2                      |
| `RULES_DATASOURCE_JDBCURL`    | `rules.datasource.jdbc-url`       | `jdbc:postgresql://localhost:5432/recommendation_rules`    | JDBC URL PostgreSQL            |
| `RULES_DATASOURCE_USERNAME`   | `rules.datasource.username`       | `rules_user`                                               | Пользователь PostgreSQL        |
| `RULES_DATASOURCE_PASSWORD`   | `rules.datasource.password`       | `rules_pass`                                               | Пароль PostgreSQL              |

### Прочие

| Переменная            | Свойство               | По умолчанию | Описание                                                       |
|-----------------------|------------------------|--------------|----------------------------------------------------------------|
| `TELEGRAM_BOT_TOKEN`  | `telegram.bot.token`   | пусто        | Токен Telegram-бота. Пусто: бот отключён, REST API работает     |
| `SERVER_PORT`         | `server.port`          | `8080`       | HTTP-порт                                                      |

> В рабочей среде обязательно переопределите пароли через переменные окружения: значения по умолчанию
> в `application.properties` предназначены только для локальной разработки.

## Запуск тестов

```bash
./mvnw test
```

Модульные тесты не требуют баз данных и Telegram.

## Типичные проблемы

| Симптом при запуске                                         | Причина и решение                                                                   |
|-------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `Connection to localhost:5432 refused`                      | PostgreSQL не запущен или неверный `RULES_DATASOURCE_JDBCURL`                       |
| `Table "TRANSACTIONS" not found`                            | Неверный путь к `transaction.mv.db` в `SPRING_DATASOURCE_URL`                       |
| `Токен Telegram-бота не задан ... бот отключён` (WARN)      | Не задан `TELEGRAM_BOT_TOKEN`; на REST API не влияет                                |
| `GET /management/info` возвращает `"version": "unknown"`    | Запуск из IDE без сборки Maven; при запуске JAR версия берётся из `pom.xml`         |

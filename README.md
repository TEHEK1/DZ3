# Система интернет-магазина (микросервисная архитектура)

Проект демонстрирует работу онлайн-магазина, реализованного на **Spring Boot** и разделённого на независимые микросервисы.

## Функциональность

1. Работа с заказами (orders-service):
   - Создание заказа
   - Просмотр списка/деталей заказа
   - Обновление статуса заказа (PENDING → PAID/REJECTED) по событиям из Kafka
2. Управление кошельком (payments-service):
   - Создание счёта пользователя
   - Пополнение баланса
   - Проверка текущего баланса
   - Проведение платежей по событиям *PaymentRequested*
3. API Gateway:
   - Единая точка входа, проксирование REST-запросов
   - Swagger-UI и агрегация OpenAPI
4. Надёжная коммуникация между сервисами через **Transactional Outbox / Inbox** и Kafka.

## Архитектура

Система состоит из следующих микросервисов:

### 1. API Gateway (`api-gateway`)
- Проксирование и маршрутизация запросов
- Агрегированная документация API (Swagger UI)
- Логирование и обработка ошибок

### 2. Orders Service (`orders-service`)
- Создание и хранение заказов (PostgreSQL)
- Публикация события *PaymentRequested* в Kafka
- Обработка события *PaymentResult* для изменения статуса заказа

### 3. Payments Service (`payments-service`)
- Хранение кошельков пользователей (PostgreSQL)
- Обработка платежей, публикация события *PaymentResult* в Kafka
- Транзакционная защита баланса (Optimistic Lock)

## Технологии
- Java 17
- Spring Boot 3.2.x
- Spring Data JPA & Hibernate
- Apache Kafka (spring-kafka)
- Spring Cloud Gateway
- PostgreSQL
- Docker & Docker Compose
- Maven
- Lombok
- Swagger / springdoc-openapi

## Требования
- JDK 17+
- Maven 3.8+
- Docker и Docker Compose (для полноценного запуска)

## Установка и запуск

### 1. Сборка проекта
```bash
./mvnw clean package -DskipTests
```

### 2. Запуск через Docker Compose
```bash
# Запуск всех сервисов и инфраструктуры
docker-compose up --build -d

# Проверка статуса контейнеров
docker compose ps

# Остановка сервисов
docker-compose down -v
```

### 3. Запуск отдельных сервисов (без Docker)
```bash
# API Gateway
cd api-gateway && ./mvnw spring-boot:run

# Orders Service
cd orders-service && ./mvnw spring-boot:run

# Payments Service
cd payments-service && ./mvnw spring-boot:run
```

## API Endpoints

Swagger UI доступен по адресу: `http://localhost:8080/swagger-ui.html`

Основные REST-эндпойнты (без учёта Gateway):

### payments-service (`localhost:8082`)
- `POST /accounts` – создать счёт
- `POST /accounts/topup` – пополнить счёт
- `GET /accounts/balance` – получить баланс

### orders-service (`localhost:8081`)
- `POST /orders` – создать заказ
- `GET /orders` – список заказов пользователя
- `GET /orders/{id}` – информация о заказе

Все запросы требуют заголовок `X-USER-ID` с идентификатором пользователя (упрощённая схема аутентификации).

## Конфигурация

Ключевые настройки находятся в `application.yml` каждого сервиса:

- `server.port` – порт приложения
- `spring.datasource.*` – параметры подключения к PostgreSQL
- `spring.kafka.bootstrap-servers` – адрес брокера Kafka

## Логирование
```bash
# Логи всех сервисов
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f orders-service
```

## Запуск тестов (Юнит и интеграционные)
```bash
./mvnw clean verify
``` 